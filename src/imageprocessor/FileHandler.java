/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author kusalh
 */
public class FileHandler {
    
    private static final String ROOT_PATH = new File("").getAbsolutePath();
    static JFileChooser filechooser = new JFileChooser(ROOT_PATH.concat("\\src\\resources"));
    static FileFilter imagefilter = new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes());
    
    public static String openFile(){
    
        filechooser.setFileFilter(imagefilter);

        int result = filechooser.showDialog(filechooser, "Open");

        if (result == 1){
            return null;
        }else{
            return filechooser.getSelectedFile().getAbsolutePath();
        }

    }
    
    public static boolean SaveAsFile(){

        FileNameExtensionFilter JPEGFilter = new FileNameExtensionFilter("JPG File", "jpg");

        filechooser.addChoosableFileFilter(JPEGFilter);
        filechooser.setFileFilter(JPEGFilter);

        int result = filechooser.showDialog(filechooser, "Save");

        if (result == 1){
                return false;
        }else{
                String path = filechooser.getSelectedFile().getAbsolutePath();
            try {
                return ImageIO.write(Editor.getEditor().getOriginalImage(), "JPEG", new File(path));
            } catch (IOException ex) {
                Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
        
	}
    
    
}
