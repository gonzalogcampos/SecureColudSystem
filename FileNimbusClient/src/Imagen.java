import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.ImageIcon;

/**
 *
 * @author Marines
 */
public class Imagen extends javax.swing.JPanel {
	
    String dir;
    String url = "/main/resources/";
    
    public Imagen() { 
        this.setSize(50, 50); // tama�o del panel por defecto
        dir = "user-default.png";
    }
    
    public Imagen(int x, int y) { 
        this.setSize(x, y); // asignar nuevo tama�o del panel 
        dir = "user-default.png";
    }
    
    public Imagen(String imagen) { 
        this.setSize(40, 40); // tama�o del panel por defecto
        dir = imagen;
    } 
    
    public Imagen(int x, int y, String imagen) { 
        this.setSize(x, y); // asignar nuevo tama�o del panel 
        dir = imagen;
    }
    

    @Override
    public void paint(Graphics grafico) { 
        Dimension height = getSize(); 

        // Se selecciona la imagen que tenemos en el paquete assets
        ImageIcon img = new ImageIcon(getClass().getResource(url+dir));
        
        // Se dibuja la imagen que tenemos en el paquete assets
        grafico.drawImage(img.getImage(), 0, 0, height.width, height.height, null); 

        setOpaque(false); 
        super.paintComponent(grafico); 
    }
}
