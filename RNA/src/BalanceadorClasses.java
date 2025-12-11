package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BalanceadorClasses {

    private double taxaMutacao;
    private Random random;

    public BalanceadorClasses(double taxaMutacao) {
        this.taxaMutacao = taxaMutacao;
        this.random = new Random();
    }

    /**
     * Recebe a base de treino no formato original (double[][][]),
     * aplica o balanceamento e retorna uma nova base no mesmo formato.
     */
    public double[][][] balancear(double[][][] baseTreino) {
        System.out.printf("\n--- Iniciando Balanceamento (%d Classes) ---\n", baseTreino[0][1].length);
        
        // 1. Converter Array para Listas por Classe para facilitar manipulação
        // Assumimos 29 classes conforme solicitado
        Map<Integer, List<double[][]>> amostrasPorClasse = new HashMap<>();
        int numClasses = baseTreino[0][1].length; // Deve ser 29

        for (double[][] amostra : baseTreino) {
            double[] y = amostra[1];
            int classeIndex = argmax(y);
            amostrasPorClasse.computeIfAbsent(classeIndex, k -> new ArrayList<>()).add(amostra);
        }

        // 2. Encontrar o alvo (classe com mais amostras)
        int maxAmostras = 0;
        for (List<double[][]> lista : amostrasPorClasse.values()) {
            if (lista.size() > maxAmostras) {
                maxAmostras = lista.size();
            }
        }
        System.out.println("Meta de balanceamento: " + maxAmostras + " amostras por classe.");

        // 3. Gerar mutantes
        List<double[][]> baseTreinoFinal = new ArrayList<>();

        for (int c = 0; c < numClasses; c++) {
            List<double[][]> lista = amostrasPorClasse.get(c);
            
            // Se a classe não existir na base de treino (0 amostras) ignora
            if (lista == null || lista.isEmpty()) continue;

            // Adiciona originais
            baseTreinoFinal.addAll(lista);

            int faltam = maxAmostras - lista.size();
            if (faltam > 0) {
                // Algoritmo Round-Robin (Circular) sobre os originais
                for (int i = 0; i < faltam; i++) {
                    double[][] original = lista.get(i % lista.size());
                    double[][] mutante = mutar(original);
                    baseTreinoFinal.add(mutante);
                }
            }
        }

        // Embaralhar o resultado final
        Collections.shuffle(baseTreinoFinal);

        System.out.println("Tamanho Final do Treino: " + baseTreinoFinal.size());
        return baseTreinoFinal.toArray(new double[0][][]);
    }

    private double[][] mutar(double[][] original) {
        double[] xOld = original[0];
        double[] yOld = original[1]; // Mantém o One-Hot original

        double[] xNew = new double[xOld.length];
        
        // Copia Y
        double[] yNew = Arrays.copyOf(yOld, yOld.length);

        // Copia X e aplica mutação
        System.arraycopy(xOld, 0, xNew, 0, xOld.length);

        for (int i = 0; i < xNew.length; i++) {
            if (i <= 2) { 
                continue; 
            }

            if (random.nextDouble() < taxaMutacao) {
                // Mutação Gaussiana Aditiva
                double ruido = random.nextGaussian() * 0.1; // Amplitude 0.1 fixa ou variável
                xNew[i] += ruido;

                // Normalização segura
                xNew[i] = Math.max(0.0, Math.min(1.0, xNew[i]));
            }
        }

        return new double[][] { xNew, yNew };
    }

    private int argmax(double[] y) {
        int max = 0;
        for (int i = 1; i < y.length; i++) {
            if (y[i] > y[max]) {
                max = i;
            }
        }
        return max;
    }
}