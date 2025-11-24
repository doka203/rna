package src;

import java.util.Random;

public class Mlp {
    private double[][] wh, wo; // Pesos da rede
    private final double ni = 0.001; // Taxa de aprendizado

    /*
     * qtdIn: quantidade de neuronios na camada de entrada
     * qtdH: quantidade de neuronios na camada intermediaria
     * qtdOut: quantidade de neuronios na camada de saida
     */
    private final int qtdIn, qtdH, qtdOut;
    private static final Random random = new Random();

    public Mlp(int qtdIn, int qtdH, int qtdOut) {
        this.qtdIn = qtdIn;
        this.qtdH = qtdH;
        this.qtdOut = qtdOut;

        this.wh = new double[qtdIn + 1][qtdH]; // qtdIn + 1 para o bias
        this.wo = new double[qtdH + 1][qtdOut];

        // Inicializa os pesos da rede com valores aleatórios
        for (int i = 0; i < qtdIn + 1; i++) {
            for (int j = 0; j < qtdH; j++) {
                this.wh[i][j] = random.nextDouble() * 0.6 - 0.3; // Valores entre -0.3 e 0.3
            }
        }
        for (int i = 0; i < qtdH + 1; i++) {
            for (int j = 0; j < qtdOut; j++) {
                this.wo[i][j] = random.nextDouble() * 0.6 - 0.3; // Valores entre -0.3 e 0.3
            }
        }
    }

    // Realiza o treinamento de uma amostra, ajustando os pesos
    public double[] treinar(double[] xin, double[] y) {
        double[] x = new double[xin.length + 1]; // Xin + bias
        for (int i = 0; i < xin.length; i++) {
            x[i] = xin[i];
        }
        x[x.length - 1] = 1; // Xin + bias

        // Saida da camada hidden (intermediaria)
        double[] H = new double[this.qtdH + 1];
        double u;
        for (int h = 0; h < this.wh[0].length; h++) {
            u = 0;
            for (int i = 0; i < wh.length; i++) {
                u += x[i] * wh[i][h];
            }
            H[h] = 1.0 / (1.0 + Math.exp(-u));
        }
        H[H.length - 1] = 1; // Bias

        // Saida da camada out
        double[] out = new double[this.qtdOut];
        for (int j = 0; j < this.wo[0].length; j++) {
            u = 0;
            for (int h = 0; h < this.wo.length; h++) {
                u += H[h] * wo[h][j];
            }
            out[j] = 1.0 / (1.0 + Math.exp(-u));
        }

        // Calcular o delta camada de saida
        double[] deltaO = new double[this.qtdOut];
        int sinal;
        for (int j = 0; j < this.qtdOut; j++) {
            sinal = (y[j] - out[j]) >= 0 ? 1 : -1;
            deltaO[j] = out[j] * (1 - out[j]) * Math.pow((y[j] - out[j]), 2) * sinal;
        }

        // Calcula a variação dos pesos
        double[] deltaH = new double[this.qtdH];
        for (int h = 0; h < this.qtdH; h++) {
            deltaH[h] = H[h] * (1 - H[h]);
            double soma = 0;
            for (int j = 0; j < this.qtdOut; j++) {
                soma += deltaO[j] * wo[h][j];
            }
            deltaH[h] *= soma;
        }

        // Ajustar pesos no wo
        for (int j = 0; j < wo[0].length; j++) {
            for (int h = 0; h < wo.length; h++) {
                wo[h][j] += this.ni * deltaO[j] * H[h];
            }
        }

        // Ajustar pesos no wh
        for (int h = 0; h < wh[0].length; h++) {
            for (int i = 0; i < wh.length; i++) {
                wh[i][h] += this.ni * deltaH[h] * x[i];
            }
        }
        return out;
    }

    public double[] testar(double[] xin, double[] y) {
        double[] x = new double[xin.length + 1]; // Xin + bias
        for (int i = 0; i < xin.length; i++) {
            x[i] = xin[i];
        }
        x[x.length - 1] = 1; // Xin + bias

        // Saida da camada hidden (intermediaria)
        double[] H = new double[this.qtdH + 1];
        double u;
        for (int h = 0; h < this.wh[0].length; h++) {
            u = 0;
            for (int i = 0; i < wh.length; i++) {
                u += x[i] * wh[i][h];
            }
            H[h] = 1.0 / (1.0 + Math.exp(-u));
        }
        H[H.length - 1] = 1; // Bias

        // Saida da camada out
        double[] out = new double[this.qtdOut];
        for (int j = 0; j < this.wo[0].length; j++) {
            u = 0;
            for (int h = 0; h < this.wo.length; h++) {
                u += H[h] * wo[h][j];
            }
            out[j] = 1.0 / (1.0 + Math.exp(-u));
        }
        return out;
    }
}