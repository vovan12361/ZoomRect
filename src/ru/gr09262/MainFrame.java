package ru_09_262;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame {
    private JPanel mainPanel = new JPanel();

    private Point point1 = null;
    private Point point2 = null;

    public MainFrame(){
        mainPanel.setBackground(Color.WHITE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 500));
        add(mainPanel);
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                point1 = e.getPoint();
                Graphics graphics = mainPanel.getGraphics();
                graphics.setXORMode(Color.WHITE);
                graphics.fillRect(-100, -100, 2, 2);
                graphics.setPaintMode();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                Graphics graphics= mainPanel.getGraphics();
                if (point2 != null) {
                    int rectWidth = point2.x - point1.x;
                    int rectHeight = point2.y - point1.y;
                    graphics.setXORMode(Color.WHITE);
                    graphics.setColor(Color.BLUE);
                    graphics.drawRect(point1.x, point1.y,
                            rectWidth, rectHeight);
                    graphics.setPaintMode();
                }
                point1 = null;
                point2 = null;
            }
        });
        mainPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                Graphics graphics = mainPanel.getGraphics();
//                point2 = e.getPoint();
                if (point2 != null) {
                    int rectWidth = point2.x - point1.x;
                    int rectHeight = point2.y - point1.y;
                    graphics.setXORMode(Color.WHITE);
                    graphics.setColor(Color.BLUE);
                    graphics.drawRect(point1.x, point1.y,
                            rectWidth, rectHeight);
                    graphics.setPaintMode();
                }
                point2 = e.getPoint();
                int rectWidth = point2.x - point1.x;
                int rectHeight = point2.y - point1.y;
                graphics.setXORMode(Color.WHITE);
                graphics.setColor(Color.BLUE);
                graphics.drawRect(point1.x, point1.y,
                        rectWidth, rectHeight);
                graphics.setPaintMode();
            }
        });
    }

    public void paint(Graphics graphics) {
        super.paint(graphics);
        Graphics g = mainPanel.getGraphics();
        g.setColor(Color.RED);
        g.fillOval(100, 100, 300, 400);
    }
}
