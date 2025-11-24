package src;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Arquivar {

    // Salva apenas os dados em formato CSV para análise em planilhas
    public static void salvarErros(double[][] erros, String nomeArquivo, int qtdH) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy_HHmm");
            String timestamp = sdf.format(new Date());
            String nomeCompleto = nomeArquivo + "_" + "qtdH" + qtdH + "_" + timestamp + ".csv";

            BufferedWriter writer = new BufferedWriter(new FileWriter(nomeCompleto));

            // Cabeçalho CSV
            writer.write("Epoca,ErroAprox_Treino,ErroClass_Treino,ErroAprox_Teste,ErroClass_Teste\n");

            // Dados
            for (int e = 0; e < erros.length; e++) {
                writer.write(String.format("%d,%.0f,%.0f,%.0f,%.0f\n", e, erros[e][0], erros[e][1], erros[e][2], erros[e][3]));
            }

            writer.close();
            System.out.println("CSV salvo em: " + nomeCompleto);

        } catch (IOException ex) {
            System.err.println("Erro ao salvar CSV: " + ex.getMessage());
        }
    }
}
