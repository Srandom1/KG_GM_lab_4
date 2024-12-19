import java.awt.*;
import java.awt.geom.Point2D;
import javax.swing.*;

public class HexahedronPanel extends JPanel {
    private HexahedronProjection projection;
    private double[] center = new double[3];
    private double size;
    private double xRotation;
    private double yRotation;
    private double zRotation;

    public HexahedronPanel(HexahedronProjection projection) {
        this.projection = projection;
    }

    @Override
    protected void paintComponent(Graphics g) {
        center = projection.getCenter();
        size = projection.getSizeValue();

        xRotation = projection.getRotationX();
        yRotation = projection.getRotationY();
        zRotation = projection.getRotationZ();

        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double[] cameraDirection = getCameraDirection();

        double[][] vertices = {
                {1, -1, 1, 1}, {-1, -1, 1, 1},
                {-1, -1, -1, 1}, {1, -1, -1, 1},
                {1, 1, 1, 1}, {-1, 1, 1, 1},
                {-1, 1, -1, 1}, {1, 1, -1, 1}
        };

        int[][] edges = {
                {3, 2, 1, 0},
                {0, 1, 5, 4},
                {0, 4, 7, 3},
                {7, 6, 2, 3},
                {1, 2, 6, 5},
                {4, 5, 6, 7}
        };

        for (double[] vertex : vertices) {
            vertex[0] *= size / 2;
            vertex[1] *= size / 2;
            vertex[2] *= size / 2;
        }

        // Масштабирование
        for (double[] vertex : vertices) {
            vertex[0] *= size / 2;
            vertex[1] *= size / 2;
            vertex[2] *= size / 2;
        }

        // Поворот
        for (double[] vertex : vertices) {
            rotate(vertex, projection.getRotationX(), projection.getRotationY(), projection.getRotationZ());
        }

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        boolean[] visibleFaces = new boolean[edges.length];

        for (int t = 0; t < edges.length; t++) {
            int[] edge = edges[t];
            // Вычисляем вектор нормали к грани с учетом искажения, получаемого во время проекции
            double[] normal = calculateDistortedNormal(vertices[edge[0]], vertices[edge[1]], vertices[edge[2]]);
            // Через векторное произведение определяем видна ли нам грань
            visibleFaces[t] = isVisible(normal, cameraDirection);
        }


        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

       
        for (int i = 0; i < visibleFaces.length; i++) {
            if (visibleFaces[i]) {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(3));
            } else {
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

    private double[] getCameraDirection() {
        return new double[]{-center[0], -center[1], -center[2]};
    }

    private void rotate(double[] vertex, double xAngel, double yAngel, double zAngel) {

        // Поворот вокруг оси X
        double newY = vertex[1] * Math.cos(xAngel) - vertex[2] * Math.sin(xAngel);
        double newZ = vertex[1] * Math.sin(xAngel) + vertex[2] * Math.cos(xAngel);
        vertex[1] = newY;
        vertex[2] = newZ;

        // Поворот вокруг оси Y
        double newX = vertex[0] * Math.cos(yAngel) + vertex[2] * Math.sin(yAngel);
        vertex[2] = -vertex[0] * Math.sin(yAngel) + vertex[2] * Math.cos(yAngel);
        vertex[0] = newX;

        // Поворот вокруг оси Z
        newX = vertex[0] * Math.cos(zAngel) - vertex[1] * Math.sin(zAngel);
        newY = vertex[0] * Math.sin(zAngel) + vertex[1] * Math.cos(zAngel);
        vertex[0] = newX;
        vertex[1] = newY;
    }

    private Point2D centralProjection(double[] vertex) {
        double k = center[2] / (center[2] - vertex[2]);
        return new Point2D.Double(k * (vertex[0] - center[0]), k * (vertex[1] - center[1]));
    }

    static boolean isVisible(double[] normal, double[] cameraDir) {
        double dotProduct = normal[0] * cameraDir[0] + normal[1] * cameraDir[1] + normal[2] * cameraDir[2];
        return dotProduct > 0;
    }

    public double[] calculateDistortedNormal(double[] p1, double[] p2, double[] p3) {
        double p1ScaleCoeff = center[2] / (center[2] - p1[2]);
        double p2ScaleCoeff = center[2] / (center[2] - p2[2]);
        double p3ScaleCoeff = center[2] / (center[2] - p3[2]);

        double[] v1 = {
                p2[0] * p2ScaleCoeff - p1[0] * p1ScaleCoeff,
                p2[1] * p2ScaleCoeff - p1[1] * p1ScaleCoeff,
                p2[2] * p2ScaleCoeff - p1[2] * p1ScaleCoeff};

        double[] v2 = {
                p3[0] * p3ScaleCoeff - p1[0] * p1ScaleCoeff,
                p3[1] * p3ScaleCoeff - p1[1] * p1ScaleCoeff,
                p3[2] * p3ScaleCoeff - p1[2] * p1ScaleCoeff};

        return new double[]{
                v1[1] * v2[2] - v1[2] * v2[1],
                v1[2] * v2[0] - v1[0] * v2[2],
                v1[0] * v2[1] - v1[1] * v2[0],
        };
    }
}
