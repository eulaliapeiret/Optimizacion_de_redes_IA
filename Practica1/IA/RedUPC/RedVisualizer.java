package IA.RedUPC;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class RedVisualizer extends JPanel {
    private final ArrayList<Nodo> nodos;
    private final int panelSize = 1000;
    private final int gridStep = 10;  // cada 10 unidades en coordenadas mundo
    private final int arrowSize = 10; // tamaño de la cabeza de la flecha
    private final int drawingMargin = 50; // margen para el dibujo
    // Variable para almacenar el nodo seleccionado (-1: ninguno)
    private int selectedNodeId = -1;

    public RedVisualizer(ArrayList<Nodo> nodos) {
        this.nodos = nodos;
        setPreferredSize(new Dimension(panelSize, panelSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Activar anti-aliasing para suavizar dibujos
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        // Calcular escala usando el margen: el área útil es width-2*margen y height-2*margen
        double scale = Math.min((width - 2 * drawingMargin) / 100.0, (height - 2 * drawingMargin) / 100.0);

        // Dibujar cuadrícula: líneas grises cada 10 unidades
        g2.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= 100; i += gridStep) {
            int pos = drawingMargin + (int) Math.round(i * scale);
            // Línea vertical
            g2.drawLine(pos, drawingMargin, pos, height - drawingMargin);
            // Línea horizontal
            g2.drawLine(drawingMargin, pos, width - drawingMargin, pos);
        }

        // Dibujar conexiones (flechas) de cada sensor a su destino
        for (Nodo nodo : nodos) {
            if (nodo instanceof NSensor) {
                NSensor sensor = (NSensor) nodo;
                // Verificar que tenga destino asignado y válido
                if (sensor.getIdDestino() != -1 && sensor.getIdDestino() < nodos.size()) {
                    int x1 = getScaledX(sensor.getSensor().getCoordX(), scale);
                    int y1 = getScaledY(sensor.getSensor().getCoordY(), scale);
                    Nodo destinoNodo = nodos.get(sensor.getIdDestino());
                    int x2 = 0, y2 = 0;
                    if (destinoNodo instanceof NSensor) {
                        NSensor destSensor = (NSensor) destinoNodo;
                        x2 = getScaledX(destSensor.getSensor().getCoordX(), scale);
                        y2 = getScaledY(destSensor.getSensor().getCoordY(), scale);
                    } else if (destinoNodo instanceof NCentro) {
                        NCentro destCentro = (NCentro) destinoNodo;
                        x2 = getScaledX(destCentro.getCentro().getCoordX(), scale);
                        y2 = getScaledY(destCentro.getCentro().getCoordY(), scale);
                    }
                    drawArrow(g2, x1, y1, x2, y2, destinoNodo instanceof NCentro);
                }
            }
        }

        // Tamaño dinámico de los nodos: radio para círculos y mitad del lado para cuadrados
        int nodeRadius = (int) Math.round(scale * 0.25); // base: mitad de la unidad escalada
        nodeRadius = Math.max(nodeRadius, 5);   // mínimo 5 píxeles
        nodeRadius = Math.min(nodeRadius, 15);  // máximo 15 píxeles

        // Dibujar cada nodo mostrando únicamente su ID en el dibujo
        for (Nodo nodo : nodos) {
            int x = 0, y = 0;
            String label = "";
            Color baseColor = Color.BLACK;
            if (nodo instanceof NSensor) {
                NSensor sensor = (NSensor) nodo;
                x = getScaledX(sensor.getSensor().getCoordX(), scale);
                y = getScaledY(sensor.getSensor().getCoordY(), scale);
                label = "S" + sensor.id;
                baseColor = Color.BLUE;
            } else if (nodo instanceof NCentro) {
                NCentro centro = (NCentro) nodo;
                x = getScaledX(centro.getCentro().getCoordX(), scale);
                y = getScaledY(centro.getCentro().getCoordY(), scale);
                label = "C" + centro.id;
                baseColor = Color.RED;
            }
            // Si este nodo está seleccionado, lo dibujamos en amarillo
            Color drawColor = (nodo.id == selectedNodeId) ? Color.YELLOW : baseColor;

            // Dibujar nodo según su forma
            if (nodo instanceof NSensor) {
                g2.setColor(drawColor);
                g2.fillOval(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
                g2.setColor(Color.BLACK);
                g2.drawOval(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
            } else if (nodo instanceof NCentro) {
                g2.setColor(drawColor);
                g2.fillRect(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
                g2.setColor(Color.BLACK);
                g2.drawRect(x - nodeRadius, y - nodeRadius, nodeRadius * 2, nodeRadius * 2);
            }
            // Dibujar la etiqueta (ID)
            g2.drawString(label, x - nodeRadius, y - nodeRadius - 5);
        }
    }

    // Transforma coordenada "mundo" en X (0 a 100) a coordenada en píxeles, considerando el margen.
    private int getScaledX(int worldX, double scale) {
        return drawingMargin + (int) Math.round(worldX * scale);
    }

    // Transforma coordenada "mundo" en Y (0 a 100) a coordenada en píxeles, considerando el margen.
    private int getScaledY(int worldY, double scale) {
        return drawingMargin + (int) Math.round(worldY * scale);
    }

    // Dibuja una flecha desde (x1, y1) hasta (x2, y2).
    private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2, boolean isCentro) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance == 0) return;  // Evitar división por cero

        // Ajustar el final de la flecha para que llegue al borde del nodo destino.
        double nodeEdge = isCentro ? 15 : 10;
        double newX2 = x2 - (dx / distance) * nodeEdge;
        double newY2 = y2 - (dy / distance) * nodeEdge;

        g2.setColor(Color.GRAY);
        g2.drawLine(x1, y1, (int) newX2, (int) newY2);

        // Dibujar cabeza de la flecha.
        double angle = Math.atan2(newY2 - y1, newX2 - x1);
        int arrowX1 = (int) (newX2 - arrowSize * Math.cos(angle - Math.PI / 6));
        int arrowY1 = (int) (newY2 - arrowSize * Math.sin(angle - Math.PI / 6));
        int arrowX2 = (int) (newX2 - arrowSize * Math.cos(angle + Math.PI / 6));
        int arrowY2 = (int) (newY2 - arrowSize * Math.sin(angle + Math.PI / 6));

        g2.setColor(Color.BLACK);
        g2.drawLine((int) newX2, (int) newY2, arrowX1, arrowY1);
        g2.drawLine((int) newX2, (int) newY2, arrowX2, arrowY2);
    }

    // Crea la tabla con la información de cada nodo (ID, Tipo, Coste, Información, X, Y).
    private JTable createNodeTable() {
        String[] columnNames = {"ID", "Tipo", "Coste", "Información", "X", "Y"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        for (Nodo nodo : nodos) {
            String id;
            String tipo;
            double coste = 0;
            double info = 0;
            int posX = 0;
            int posY = 0;
            if (nodo instanceof NSensor) {
                NSensor sensor = (NSensor) nodo;
                id = "S" + sensor.id;
                tipo = "Sensor";
                coste = sensor.getCoste();
                info = sensor.getInfoEnviable();
                posX = sensor.getSensor().getCoordX();
                posY = sensor.getSensor().getCoordY();
            } else if (nodo instanceof NCentro) {
                NCentro centro = (NCentro) nodo;
                id = "C" + centro.id;
                tipo = "Centro";
                coste = centro.getCoste();
                info = centro.informacionRecibida;
                posX = centro.getCentro().getCoordX();
                posY = centro.getCentro().getCoordY();
            } else {
                id = "N" + nodo.id;
                tipo = "Desconocido";
            }
            Object[] row = {id, tipo, String.format("%.2f", coste), String.format("%.2f", info), posX, posY};
            model.addRow(row);
        }
        return new JTable(model);
    }

    // Crea el panel de botones: un botón por cada nodo.
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        // Usamos BoxLayout vertical para listar los botones
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        for (Nodo nodo : nodos) {
            String buttonLabel = (nodo instanceof NSensor) ? "S" + nodo.id : "C" + nodo.id;
            JButton button = new JButton(buttonLabel);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            // Añadimos un ActionListener que marca el nodo seleccionado
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedNodeId = nodo.id;
                    repaint();
                }
            });
            buttonPanel.add(button);
        }
        return buttonPanel;
    }

    // Método estático para visualizar la red, la tabla de nodos y el panel de botones en un mismo JFrame.
    public static void visualizarRedConControles(ArrayList<Nodo> nodos) {
        JFrame frame = new JFrame("Visualización de la Red y Datos de Nodos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Panel de dibujo en el centro
        RedVisualizer visualizer = new RedVisualizer(nodos);
        frame.add(visualizer, BorderLayout.CENTER);

        // Tabla de información en la parte sur
        JTable table = visualizer.createNodeTable();
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(1000, 200));
        frame.add(tableScroll, BorderLayout.SOUTH);

        // Panel de botones en el este, en un JScrollPane por si hay muchos nodos
        JPanel buttonPanel = visualizer.createButtonPanel();
        JScrollPane buttonScroll = new JScrollPane(buttonPanel);
        buttonScroll.setPreferredSize(new Dimension(150, 1000));
        frame.add(buttonScroll, BorderLayout.EAST);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
