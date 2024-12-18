import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

public class HexahedronProjection extends JFrame {
    private double rotationX = 0, rotationY = 0, rotationZ = 0, size = 100;
    private double[] center = {0, 0, -500};

    public HexahedronProjection() {
        setTitle("Проецирование гексаэдра");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.WEST);

        HexahedronPanel hexahedronPanel = new HexahedronPanel();
        add(hexahedronPanel, BorderLayout.CENTER);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createSlider("Размер", 50, 300, (int) size, e -> {
            size = ((JSlider) e.getSource()).getValue();
            repaint();
        }));
        panel.add(createSlider("Поворот X", 0, 360, 0, e -> {
            rotationX = Math.toRadians(((JSlider) e.getSource()).getValue());
            repaint();
        }));
        panel.add(createSlider("Поворот Y", 0, 360, 0, e -> {
            rotationY = Math.toRadians(((JSlider) e.getSource()).getValue());
            repaint();
        }));
        panel.add(createSlider("Поворот Z", 0, 360, 0, e -> {
            rotationZ = Math.toRadians(((JSlider) e.getSource()).getValue());
            repaint();
        }));
        panel.add(createSlider("Центр X", -500, 500, 0, e -> {
            center[0] = ((JSlider) e.getSource()).getValue();
            repaint();
        }));
        panel.add(createSlider("Центр Y", -500, 500, 0, e -> {
            center[1] = ((JSlider) e.getSource()).getValue();
            repaint();
        }));
        panel.add(createSlider("Центр Z", -1000, 0, -1000, e -> {
            center[2] = ((JSlider) e.getSource()).getValue();
            repaint();
        }));

        return panel;
    }

    private JSlider createSlider(String label, int min, int max, int initial, ActionListener listener) {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, initial);
        slider.setMajorTickSpacing((max - min) / 10);
        slider.setPaintTicks(true);
        slider.addChangeListener(e -> listener.actionPerformed(new ActionEvent(slider, ActionEvent.ACTION_PERFORMED, null)));
        slider.setBorder(BorderFactory.createTitledBorder(label));
        return slider;
    }

    private class HexahedronPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double[] cameraDirection = getCameraDirection();

            // Вершины гексаэдра
            double[][] vertices = {
                    {0.4, -0.4, 0.4, 1}, {-0.4, -0.4, 0.4, 1},
                    {-0.4, -0.4, -0.4, 1}, {0.4, -0.4, -0.4, 1},
                    {0.4, 0.4, 0.4, 1}, {-0.4, 0.4, 0.4, 1},
                    {-0.4, 0.4, -0.4, 1}, {0.4, 0.4, -0.4, 1}
            };

            //Грани гексаэдера
            int[][] edges = {
                    {3, 2, 1, 0},
                    {0, 1, 5, 4},
                    {0, 4, 7, 3},
                    {7, 6, 2, 3},
                    {1, 2, 6, 5},
                    {4, 5, 6, 7}
            };

            // Масштабирование
            for (double[] vertex : vertices) {
                vertex[0] *= size / 2;
                vertex[1] *= size / 2;
                vertex[2] *= size / 2;
            }

            // Поворот
            for (double[] vertex : vertices) {
                rotate(vertex, rotationX, rotationY, rotationZ);
            }

            // Центр панели
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            boolean[] visibleFaces = new boolean[edges.length];

            for (int t = 0; t < edges.length; t++) {
                int[] edge = edges[t];
                // Вычисляем вектор нормали поверхности грани
                double[] normal = calculateNormal(vertices[edge[0]], vertices[edge[1]], vertices[edge[2]]);
                // Через векторное произведение определяем
                // сонаправлен ли вектор нормали и вектор взгляда наблюдателя
                visibleFaces[t] = isVisible(normal, cameraDirection);
            }


            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));

            // Отрисовка проекций
            for (int i = 0; i < visibleFaces.length; i++) {
                if (visibleFaces[i]) {
                    // Если грань видимая задаем, что ее ребра будем рисовать
                    // черным цветом и жирными линиями
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(3));
                } else {
                    // Если грань невидимая, рисуем тонкими линиями, серым цветом
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5, new float[]{10, 20}, 1));
                }

                // Пробегаемся по всем ребрам грани и рисуем их
                for (int j = 0; j < edges[i].length; j++) {
                    int k = (j + 1) % edges[i].length;
                    Point2D p1 = centralProjection(vertices[edges[i][j]]);
                    Point2D p2 = centralProjection(vertices[edges[i][k]]);
                    g2d.drawLine(centerX + (int) p1.getX(), centerY + (int) p1.getY(), centerX + (int) p2.getX(), centerY + (int) p2.getY());
                }
            }
        }

        private double[] getCameraDirection(){
            return new double[]{-center[0], -center[1], -center[2]};
        }
        private void rotate(double[] vertex, double angleX, double angleY, double angleZ) {
            // Поворот вокруг оси X
            double tempY = vertex[1] * Math.cos(angleX) - vertex[2] * Math.sin(angleX);
            double tempZ = vertex[1] * Math.sin(angleX) + vertex[2] * Math.cos(angleX);
            vertex[1] = tempY;
            vertex[2] = tempZ;

            // Поворот вокруг оси Y
            double tempX = vertex[0] * Math.cos(angleY) + vertex[2] * Math.sin(angleY);
            vertex[2] = -vertex[0] * Math.sin(angleY) + vertex[2] * Math.cos(angleY);
            vertex[0] = tempX;

            // Поворот вокруг оси Z
            tempX = vertex[0] * Math.cos(angleZ) - vertex[1] * Math.sin(angleZ);
            vertex[1] = vertex[0] * Math.sin(angleZ) + vertex[1] * Math.cos(angleZ);
            vertex[0] = tempX;
        }

        private Point2D centralProjection(double[] vertex) {
            double k = center[2] / (center[2] - vertex[2]);
            return new Point2D.Double(k * (vertex[0] - center[0]), k * (vertex[1] - center[1]));
        }

        static boolean isVisible(double[] normal, double[] cameraDir) {
            double dotProduct = normal[0] * cameraDir[0] + normal[1] * cameraDir[1] + normal[2] * cameraDir[2];
            return dotProduct > 0;
        }

        static double[][] multiplyMatrix(double[][] firstMatrix, double[][] SecondMatrix) {
            if (firstMatrix[0].length != SecondMatrix.length) {
                throw new IllegalArgumentException("Матрицы нельзя перемножить");
            }
            double[][] result = new double[firstMatrix.length][SecondMatrix[0].length];
            for (int i = 0; i < result.length; i++) {
                for (int j = 0; j < result[0].length; j++) {
                    for (int k = 0; k < firstMatrix[0].length; k++) {
                        result[i][j] += firstMatrix[i][k] * SecondMatrix[k][j];
                    }
                }
            }
            return result;
        }

        static double[] calculateNormal(double[] p1, double[] p2, double[] p3) {
            double[] v1 = {p2[0] - p1[0], p2[1] - p1[1], p2[2] - p1[2]};
            double[] v2 = {p3[0] - p1[0], p3[1] - p1[1], p3[2] - p1[2]};

            return new double[]{
                    v1[1] * v2[2] - v1[2] * v2[1],
                    v1[2] * v2[0] - v1[0] * v2[2],
                    v1[0] * v2[1] - v1[1] * v2[0],
            };
        }


    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HexahedronProjection().setVisible(true));
    }
}


