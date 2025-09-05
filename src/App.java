import javax.swing.*;

public class App { 
    public static void main(String[] args) throws Exception {
        int boardWidth = 360;
        int boardHeight = 640;

        JFrame frame = new JFrame("Cyber Car Flying");
        //frame.setVisible(true);
        frame.setSize(boardWidth,boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //instance
        CyberCar cyberCar = new CyberCar();
        frame.add(cyberCar);
        frame.pack();
        cyberCar.requestFocus();
        frame.setVisible(true);
    }
}
