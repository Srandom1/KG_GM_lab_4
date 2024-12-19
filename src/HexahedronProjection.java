import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class HexahedronProjection extends JFrame {
    private double rotationX = 0, rotationY = 0, rotationZ = 0, size = 20;
    private double[] center = {0, 0, 500};
    HashMap<String, JSpinner> spinnerMap = new HashMap<>();

    public HexahedronProjection() {
        setTitle("Проецирование гексаэдера");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.WEST);

        HexahedronPanel hexahedronPanel = new HexahedronPanel(this);
        hexahedronPanel.setFocusable(true);
        add(hexahedronPanel, BorderLayout.CENTER);

        HexahedronProjection instance = this;

        this.requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            boolean isXPressed = false;
            boolean isZPressed = false;
            boolean isCPressed = false;

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                int sizeStep = 5;
                int step = 5;
                int smoothTurning = 60;

                if (keyCode == KeyEvent.VK_X) {
                    isXPressed = true;
                } else if (keyCode == KeyEvent.VK_Z) {
                    isZPressed = true;
                } else if (keyCode == KeyEvent.VK_C) {
                    isCPressed = true;
                }

                if (isZPressed) {
                    if (keyCode == KeyEvent.VK_LEFT) {
                        rotationZ -= Math.PI / smoothTurning;
                    } else if (keyCode == KeyEvent.VK_RIGHT) {
                        rotationZ += Math.PI / smoothTurning;
                    }
                } else if (isXPressed) {
                    if (keyCode == KeyEvent.VK_LEFT) {
                        rotationX -= Math.PI / smoothTurning;
                    } else if (keyCode == KeyEvent.VK_RIGHT) {
                        rotationX += Math.PI / smoothTurning;
                    }
                } else if (isCPressed) {
                    if (keyCode == KeyEvent.VK_LEFT) {
                        rotationY -= Math.PI / smoothTurning;
                    } else if (keyCode == KeyEvent.VK_RIGHT) {
                        rotationY += Math.PI / smoothTurning;
                    }
                }

                if (isXPressed || isCPressed || isZPressed) {
                    updateSpinners();
                    repaint();
                    return;
                }

                if (keyCode == KeyEvent.VK_LEFT) {
                    center[0] -= step;
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    center[0] += step;
                } else if (keyCode == KeyEvent.VK_UP) {
                    center[1] -= step;
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    center[1] += step;
                } else if (keyCode == KeyEvent.VK_PAGE_UP) {
                    size += sizeStep;
                } else if (keyCode == KeyEvent.VK_PAGE_DOWN) {
                    size -= sizeStep;
                }

                updateSpinners();
                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_X) {
                    isXPressed = false;
                } else if (keyCode == KeyEvent.VK_Z) {
                    isZPressed = false;
                } else if (keyCode == KeyEvent.VK_C) {
                    isCPressed = false;
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                instance.requestFocusInWindow();
            }
        });
        hexahedronPanel.requestFocusInWindow();
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createLabeledSpinner("Размер", Integer.MIN_VALUE, Integer.MAX_VALUE, (int) size, e -> {
            size = (Integer) ((JSpinner) e.getSource()).getValue();
            repaint();
        }));
        panel.add(createLabeledSpinner("Поворот X", Integer.MIN_VALUE, Integer.MAX_VALUE, (int) Math.toDegrees(rotationX), e -> {
            rotationX = Math.toRadians((Integer) ((JSpinner) e.getSource()).getValue());
            repaint();
        }));
        panel.add(createLabeledSpinner("Поворот Y", Integer.MIN_VALUE, Integer.MAX_VALUE,(int) Math.toDegrees(rotationY), e -> {
            rotationY = Math.toRadians((Integer) ((JSpinner) e.getSource()).getValue());
            repaint();
        }));
        panel.add(createLabeledSpinner("Поворот Z", Integer.MIN_VALUE, Integer.MAX_VALUE, (int) Math.toDegrees(rotationZ), e -> {
            rotationZ = Math.toRadians((Integer) ((JSpinner) e.getSource()).getValue());
            repaint();
        }));
        panel.add(createLabeledSpinner("Центр X", Integer.MIN_VALUE, Integer.MAX_VALUE, (int) center[0], e -> {
            center[0] = (Integer) ((JSpinner) e.getSource()).getValue();
            repaint();
        }));
        panel.add(createLabeledSpinner("Центр Y", Integer.MIN_VALUE, Integer.MAX_VALUE, (int) center[1], e -> {
            center[1] = (Integer) ((JSpinner) e.getSource()).getValue();
            repaint();
        }));
        panel.add(createLabeledSpinner("Центр Z", Integer.MIN_VALUE, Integer.MAX_VALUE, (int) center[2], e -> {
            center[2] = (Integer) ((JSpinner) e.getSource()).getValue();
            repaint();
        }));

        JButton helpButton = new JButton("Руководство");
        helpButton.setSize(200, 30);
        helpButton.addActionListener(e -> showHelpDialog());
        panel.setSize(300, this.getHeight());
        panel.add(helpButton);


        return panel;
    }

    private JPanel createLabeledSpinner(String label, int min, int max, int initial, ChangeListener listener) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel jLabel = new JLabel(label);
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initial, min, max, 1));
        spinner.addChangeListener(listener);

        panel.add(jLabel);
        panel.add(spinner);
        spinner.setPreferredSize(new Dimension(45, 15));
        spinnerMap.put(label, spinner);

        return panel;
    }

    private void updateSpinners() {
        for (String label : spinnerMap.keySet()) {
            switch (label) {
                case "Размер":
                    spinnerMap.get(label).setValue((int) size);
                    break;
                case "Поворот X":
                    spinnerMap.get(label).setValue((int) Math.toDegrees(rotationX));
                    break;
                case "Поворот Y":
                    spinnerMap.get(label).setValue((int) Math.toDegrees(rotationY));
                    break;
                case "Поворот Z":
                    spinnerMap.get(label).setValue((int) Math.toDegrees(rotationZ));
                    break;
                case "Центр X":
                    spinnerMap.get(label).setValue((int) center[0]);
                    break;
                case "Центр Y":
                    spinnerMap.get(label).setValue((int) center[1]);
                    break;
                case "Центр Z":
                    spinnerMap.get(label).setValue((int) center[2]);
                    break;
            }
        }
    }

    public double getRotationX() {
        return rotationX;
    }

    public double getRotationY() {
        return rotationY;
    }

    public double getRotationZ() {
        return rotationZ;
    }

    public double getSizeValue() {
        return size;
    }

    public double[] getCenter() {
        return center;
    }

    private void showHelpDialog() {
        String message = "Руководство к использованию\n" +
                "Управление: \n" +
                "1. Используйте стрелки для изменения центра проекции (положения гексаэдера).\n" +
                "2. При зажатых клавишах Z,X,C и использовании стрелок, куб будет вращатся вокург соотве" +
                "тствующей оси.\n (Кнопка C отвечает за Y. Была выбрана, так как находится рядом с X и Z на клавиатуре) \n" +
                "3. PAGE UP и PAGE DOWN изменяют размер.\n" +
                "4. Спиннеры слева изменяют параметры вручную.\n";
        JOptionPane.showMessageDialog(this, message, "Руководство", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HexahedronProjection projection = new HexahedronProjection();
            projection.setVisible(true);
        });
    }
}


