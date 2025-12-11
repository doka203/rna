package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeitorAbalone {
    public double[][][] baseTreino;
    public double[][][] baseTeste;

    public LeitorAbalone(double[][][] baseTreino, double[][][] baseTeste) {
        this.baseTreino = baseTreino;
        this.baseTeste = baseTeste;
    }

    public static double[][][] lerBase(String caminhoArquivo) {
        List<double[][]> baseList = new ArrayList<>();

        // Constantes de Normalização
        double[] minValues = { 0.075000, 0.055000, 0.000000, 0.002000, 0.001000, 0.000500, 0.001500 };
        double[] maxValues = { 0.815000, 0.650000, 1.130000, 2.825500, 1.488000, 0.760000, 1.005000 };

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                // Divide a linha pelos separadores
                String[] valores = linha.trim().split(",");

                if (valores.length == 9) {
                    double[] entradas = new double[10];
                    for (int i = 0; i <= 7; i++) {
                        if (valores[i].equals("M")) {
                            entradas[0] = 1;
                            entradas[1] = 0;
                            entradas[2] = 0;
                        } else if (valores[i].equals("F")) {
                            entradas[0] = 0;
                            entradas[1] = 1;
                            entradas[2] = 0;
                        } else if (valores[i].equals("I")) {
                            entradas[0] = 0;
                            entradas[1] = 0;
                            entradas[2] = 1;
                        } else {
                            // Normaliza os outros atributos numéricos
                            double valor = Double.parseDouble(valores[i]);
                            entradas[i + 2] = normalizar(valor, minValues[i - 1], maxValues[i - 1]);
                        }
                    }

                    int rings = (int) Double.parseDouble(valores[8]);

                    // Codificação One-Hot para 29 classes (1 a 29 anéis)
                    double[] saidas = new double[29];
                    for (int j = 0; j < 29; j++) {
                        saidas[j] = 0.005; // Valor baixo para classes não ativas
                    }
                    if (rings >= 1 && rings <= 29) {
                        saidas[rings - 1] = 0.995; // Valor alto para classe ativa (rings-1 porque array começa em 0)
                    }

                    baseList.add(new double[][] { entradas, saidas });
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo da base de dados: " + e.getMessage());
            return null;
        }

        return filtrarClasses(baseList, 4).toArray(new double[0][][]);
    }

    // 4) Normalização Min-Max
    private static double normalizar(double valor, double min, double max) {
        double res = (valor - min) / (max - min);
        // Garante que fique entre 0 e 1
        return Math.max(0.0, Math.min(1.0, res));
    }

    private static List<double[][]> filtrarClasses(List<double[][]> dados, int minAmostras) {
        Map<Integer, Integer> contagem = new HashMap<>();
        for (double[][] d : dados) {
            int c = argmax(d[1]);
            contagem.put(c, contagem.getOrDefault(c, 0) + 1);
        }

        List<double[][]> classesFiltradas = new ArrayList<>();
        int amostrasRemovidas = 0;

        System.out.println("--- Filtragem de Classes (Min: " + minAmostras + ") ---");
        for (double[][] d : dados) {
            int c = argmax(d[1]);
            if (contagem.get(c) >= minAmostras) {
                classesFiltradas.add(d);
            } else {
                amostrasRemovidas++;
            }
        }
        System.out.println("Amostras removidas: " + amostrasRemovidas + " | Restantes: " + classesFiltradas.size());
        return classesFiltradas;
    }

    public static double[][][] lerBaseOrdenada(String path) {
        double[][][] base = lerBase(path);
        if (base != null) {
            Arrays.sort(base, (amostra1, amostra2) -> Double.compare(argmax(amostra1[1]), argmax(amostra2[1])));
        }
        return base;
    }

    public static LeitorAbalone separarBase(double[][][] baseCompleta) {
        if (baseCompleta == null) {
            return null;
        }

        // Separar amostras por classe
        List<List<double[][]>> classes = new ArrayList<>();
        for (int i = 0; i < 29; i++) {
            classes.add(new ArrayList<>());
        }

        // Classificar cada amostra na classe correspondente
        for (double[][] amostra : baseCompleta) {
            // Encontrar qual classe está ativa (valor 0.995 no vetor one-hot)
            int classeAtiva = -1;
            for (int j = 0; j < 29; j++) {
                if (amostra[1][j] > 0.5) { // 0.995 > 0.5
                    classeAtiva = j;
                    break;
                }
            }

            // Adicionar à classe correspondente (se válida)
            if (classeAtiva >= 0 && classeAtiva < 29) {
                classes.get(classeAtiva).add(amostra);
            }
        }

        // Criar listas para treino e teste
        List<double[][]> treinoList = new ArrayList<>();
        List<double[][]> testeList = new ArrayList<>();

        // Para cada classe, separar 75% para treino e 25% para teste
        for (int classe = 0; classe < 29; classe++) {
            List<double[][]> amostrasClasse = classes.get(classe);

            if (amostrasClasse.size() > 0) {
                // Embaralhar as amostras da classe
                Collections.shuffle(amostrasClasse);

                // Calcular 75% para treino
                int tamanhoTreino = (int) Math.round(amostrasClasse.size() * 0.75);

                // Adicionar 75% ao treino
                for (int i = 0; i < tamanhoTreino; i++) {
                    treinoList.add(amostrasClasse.get(i));
                }

                // Adicionar os 25% restantes ao teste
                for (int i = tamanhoTreino; i < amostrasClasse.size(); i++) {
                    testeList.add(amostrasClasse.get(i));
                }
            }
        }

        // Converter para arrays
        double[][][] baseTreino = treinoList.toArray(new double[0][][]);
        double[][][] baseTeste = testeList.toArray(new double[0][][]);

        return new LeitorAbalone(baseTreino, baseTeste);
    }

    private static int argmax(double[] y) {
        int max = 0;
        for (int i = 1; i < y.length; i++) {
            if (y[i] > y[max]) {
                max = i;
            }
        }
        return max;
    }
}