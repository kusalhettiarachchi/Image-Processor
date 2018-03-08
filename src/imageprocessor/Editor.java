/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Stack;
import javax.imageio.ImageIO;

/**
 *
 * @author kusalh
 */
public class Editor {
    
    private static Editor Editor;
    String imagepath;
    //Stack for storing the temporal images while processing
    List<BufferedImage> versions;
    Deque<BufferedImage> undoStack;
    Deque<BufferedImage> redoStack;
    HashMap<List<Integer>,BufferedImage> scaledImages;
    BufferedImage image = null;
    BufferedImage originalImage;
    static boolean processed = false;

    public Editor(String path){
        this.imagepath = path;
        this.versions = new ArrayList<>();
        this.undoStack = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
        this.scaledImages = new HashMap<>();

        //Read the image file and add it to the image stack
        try {
            this.image = ImageIO.read(new File(path));
            originalImage = image;
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.versions.add(this.image);
        this.undoStack.push(this.image);
    }
    
    public static Editor getEditor() {
        return Editor;
    }

    public static void setEditor(Editor editor) {
        Editor = editor;
    }

    public BufferedImage getOriginalImage() {
        return originalImage;
    }
    
    public void undo(){
//        int index = versions.indexOf(image);
//        try{
//            this.image = versions.get(index - 1);
//        }catch(NullPointerException e){
//            System.out.println("Cannot Undo!");
//        }

        try{
            redoStack.push(undoStack.pop());
            this.image = redoStack.peek();
        }catch(Exception e){
            System.out.println("Empty undo stack!");
        }
    }
    
    public void redo(){ 
//        int index = versions.indexOf(image);
//        try{
//            this.image = versions.get(index + 1);
//        }catch(NullPointerException e){
//            System.out.println("Cannot Redo");
//        }

        try{
            undoStack.push(redoStack.pop());
            this.image = undoStack.peek();
        }catch(Exception e){
            System.out.println("Empty redo stack!");
        }
    }
    
    
    public String getImagepath() {
        return imagepath;
    }

    public BufferedImage getImage() {
        return image;
    }
    
    public void addImage(BufferedImage newImage){
        undoStack.push(newImage);
        this.image = newImage;
    }
    
//    Pixel operations 
    
    private Color getColorAt(int x, int y){
        return new Color(this.image.getRGB(x, y));
    }
    
    private Color getOriginalColorAt(int x, int y){
        return new Color(this.originalImage.getRGB(x, y));
    }

    private int[] getSize(){
        return new int[]{this.image.getWidth(), this.image.getHeight()};
    }
    
    private int[] getOriginalSize(){
        return new int[]{this.originalImage.getWidth(), this.originalImage.getHeight()};
    }
	
    private int getColorIntensity(Color currentColor){
        return (currentColor.getBlue()+currentColor.getGreen()+currentColor.getRed())/3;           
    }
    
//    Point Operations
    public BufferedImage getGrayScale(int type){
        
        int [] size = this.getSize();
	BufferedImage newImage = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_RGB);
        processed = true;
        
        switch(type){
        
            case 0:
                return getAverageGrayScale(size,newImage);
            
            case 1:
                return getLightnessGrayScale(size,newImage);
                
            case 2:
                return getLuminosityGrayScale(size, newImage);
             
        }
        
        return null;
    }
    
    public BufferedImage getAverageGrayScale(int[] size, BufferedImage newImage){
        
        for(int j=0; j<size[1]; j++){
            for(int i=0; i<size[0]; i++ ){
                int avg  = (this.getColorAt(i, j).getBlue() + this.getColorAt(i, j).getGreen() + this.getColorAt(i, j).getRed())/3;
                Color avgColor = new Color(avg, avg, avg);
                newImage.setRGB(i, j, avgColor.getRGB());
            }
        }
        return newImage;
    }
    
    public BufferedImage getLightnessGrayScale(int[] size, BufferedImage newImage){
        
        for(int j=0; j<size[1]; j++){
            for(int i=0; i<size[0]; i++ ){
                Color currentColor = this.getColorAt(i, j);

                //get max and min values
                int max = Math.max(currentColor.getBlue(), Math.max(currentColor.getGreen(), currentColor.getRed()));
                int min = Math.min(currentColor.getBlue(), Math.min(currentColor.getGreen(), currentColor.getRed()));

                int avg = (max + min)/2;
                Color avgColor = new Color(avg, avg, avg);

                newImage.setRGB(i, j, avgColor.getRGB());
            }
        }
        return newImage;
        
    }
    
    public BufferedImage getLuminosityGrayScale(int[] size, BufferedImage newImage){
        for(int j=0; j<size[1]; j++){
            for(int i=0; i<size[0]; i++ ){
                Color currentColor = this.getColorAt(i, j);

                //weighted avg
                int avg = (int) ((0.21*currentColor.getRed()) + (0.72*currentColor.getGreen()) + (0.07*currentColor.getBlue()));

                Color avgColor = new Color(avg, avg, avg);

                newImage.setRGB(i, j, avgColor.getRGB());
            }
        }
        return newImage;
    
        
    
    }
    
    public BufferedImage getMeanFilter(){
    
        int [] size = this.getSize();
        BufferedImage newImage = new BufferedImage((int)(size[0]), (int)(size[1]), BufferedImage.TYPE_INT_RGB);
        processed = true;
        
        final int[][] CONVO_MASK = new int[] [] {new int[]{1,2,1}, new int[]{2,4,2}, new int[]{1,2,1}};
        final int DIVISOR = 16;
        

        for(int j=0; j<size[1]; j++){
            for(int i=0; i<size[0]; i++){
                
                //convolution process
                double colorRed=0;
                double colorGreen=0;
                double colorBlue=0;

                try{
                    for(int r=0; r<3; r++){
                        for(int c=0; c<3; c++){
                            
                                colorRed = colorRed + ((double)(getColorAt(i-1+c, j-1+r).getRed()*CONVO_MASK[r][c]))/DIVISOR;
                                colorGreen = colorGreen + ((double)(getColorAt(i-1+c, j-1+r).getGreen()*CONVO_MASK[r][c]))/DIVISOR;
                                colorBlue = colorBlue + ((double)(getColorAt(i-1+c, j-1+r).getBlue()*CONVO_MASK[r][c]))/DIVISOR;
                        }
                    }
                }catch(ArrayIndexOutOfBoundsException e){
                    colorRed = (double) getColorAt(i, j).getRed();
                    colorBlue = (double) getColorAt(i, j).getBlue();
                    colorGreen = (double) getColorAt(i, j).getGreen();
                }
                

                Color newColor = new Color((int)(colorRed), (int)(colorGreen), (int)(colorBlue));
                Color currentColor = getColorAt(i, j);
                
                newImage.setRGB(i, j, newColor.getRGB());
            }
        }
        return newImage;
    }
    
    public BufferedImage getMedianFilter(){
        
        int [] size = this.getSize();
	BufferedImage newImage = new BufferedImage((int)(size[0]), (int)(size[1]), BufferedImage.TYPE_INT_RGB);
        processed = true;
        
        for(int j=0; j<size[1]; j++){
            for(int i=0; i<size[0]; i++){
                
                HashMap<Integer,Color> colorIntensityMap = new HashMap<>();
                
                try{
                    for(int row=0; row<3; row++){
                        for(int col=0; col<3; col++){
                            Color color = getColorAt(i-1+col, j-1+row);
                            Integer intensity = getColorIntensity(color);
                            colorIntensityMap.putIfAbsent(intensity, color);
                        }
                    }
                    Object[] temp = colorIntensityMap.keySet().toArray();
                    ArrayList<Integer> intensityOrder = new ArrayList<>();
                    for (Object object : temp) {
                        
                        intensityOrder.add((int)object);
                        
                    }
                    intensityOrder.sort(null);
                    Integer medianIntensity = intensityOrder.get((intensityOrder.size() - 1) / 2);

                    newImage.setRGB(i, j, colorIntensityMap.get(medianIntensity).getRGB());
                    
                }catch(ArrayIndexOutOfBoundsException e){
                
                    newImage.setRGB(i, j, getColorAt(i, j).getRGB());
                }   
            }
        }	
        return newImage;
    }
    
    public BufferedImage getAlphaTrimFilter(int p){
        /*
         * value of p decides the type of the filter to apply 
         * window size fixed and it is 9. N=9
         * */
        int [] size = this.getSize();
        BufferedImage newImage = new BufferedImage((int)(size[0]), (int)(size[1]), BufferedImage.TYPE_INT_RGB);
        processed = true;
        
        for(int j=0; j<size[1]; j++){
            for(int i=0; i<size[0]; i++){
                        //body
                ArrayList <Integer> intensityBuffer = new ArrayList<Integer>();
                HashMap<Integer,Color> colorIntensityMap = new HashMap<>();
                for(int row=0; row<3; row++){
                    for(int col=0; col<3; col++){

                        try{
                            Color color = getColorAt(i-1+col, j-1+row);
                            Integer intensity = getColorIntensity(color);

                            colorIntensityMap.put(intensity, color);
                            intensityBuffer.add(intensity);
                            intensityBuffer.sort(null);
                        }
                        catch(ArrayIndexOutOfBoundsException e){
                            
                        }
                    }
                }                   
                    int divisor = 9-2*p;
                   
                    double red = 0;
                    double green = 0;
                    double blue = 0;

                    if((9-p) <= intensityBuffer.size()){

                        try{
                            for (int t=p; t<9-p; t++){
                                red += colorIntensityMap.get(intensityBuffer.get(t)).getRed();
                                green += colorIntensityMap.get(intensityBuffer.get(t)).getGreen();
                                blue += colorIntensityMap.get(intensityBuffer.get(t)).getBlue();
                            }
                            red = red/divisor;
                            green = green/divisor;
                            blue = blue/divisor;
                            Color newColor = new Color((int)red, (int)green, (int)blue);
                            newImage.setRGB(i, j, newColor.getRGB());
                        }catch(ArrayIndexOutOfBoundsException e){
                            newImage.setRGB(i, j, getColorAt(i, j).getRGB());
                        }
                    }else{

                        newImage.setRGB(i, j, getColorAt(i, j).getRGB());

                    }
                }
            }
        return newImage;
    }
    
    public BufferedImage scaleDown(){
        
        int divisor = 2;
        int [] size = this.getSize();
        List<Integer> newSize = new ArrayList<>(Arrays.asList((int)(size[0]/divisor)/10, (int)(size[1]/divisor)/10));
        BufferedImage newImage = new BufferedImage((int)(size[0]/divisor), (int)(size[1]/divisor), BufferedImage.TYPE_INT_RGB);

        if(scaledImages.containsKey(newSize) && !processed){
        
            return scaledImages.get(newSize);
                    
        }else{

            for(int j=0; j<newImage.getHeight(); j++){
                for(int i=0; i<newImage.getWidth(); i++){
                    try{
                        Color currentColor = getColorAt((int)(i*divisor), (int)(j*divisor));
                        newImage.setRGB(i, j, currentColor.getRGB());
                    }catch(Exception e){
                        Color currentColor = getColorAt((int)((i-1)*divisor), (int)((j-1)*divisor));
                        newImage.setRGB(i, j, currentColor.getRGB());
                    }
                }
            }
            scaledImages.put(newSize,newImage);
//            processed = false;
            return newImage;
        }
    }
    
    public BufferedImage scaleUp(int type){
        /*
         * Nearest Neighbour method
         * */
        int [] size = this.getSize();
        int multiplier = 2;
        
        List<Integer> newSize = new ArrayList<>(Arrays.asList(size[0]*multiplier/10, size[1]*multiplier/10));
        BufferedImage newImage = new BufferedImage(size[0]*multiplier, size[1]*multiplier, BufferedImage.TYPE_INT_RGB);
        
        if(scaledImages.containsKey(newSize) && !processed){
        
            return scaledImages.get(newSize);
                    
        }else{
            if (type==0 | type==1){
                //Nearest Neighbour method
                for(int j=0; j<size[1]; j++){
                    for(int i=0; i<size[0]; i++){
                        Color currentColor = getColorAt(i, j);
                        for(int row=0; row<multiplier; row++){
                            for(int col=0; col<multiplier; col++){
                                newImage.setRGB(i*multiplier + col, j*multiplier + row, currentColor.getRGB());
                            }
                        }
                    }
                }
            }
            scaledImages.put(newSize,newImage);
//            processed = false;
            return newImage;
        }
    }
    
    
    public BufferedImage getRotated(int direction){
        /*
         * rotation = 0 clockwise;
         * rotation = 1 counter clockwise;
         * */
        
        int [] size = this.getSize();
        BufferedImage newImage = new BufferedImage((int)(size[1]), (int)(size[0]), BufferedImage.TYPE_INT_RGB);
        processed = true;
        
        switch(direction){
            
            case 0:
                for (int j=0; j<size[1]; j++){
                        for(int i=0; i<size[0]; i++){
                                newImage.setRGB(size[1]-1-j, i, getColorAt(i, j).getRGB());
                        }
                }
                return newImage;
            
            case 1:
                for (int j=0; j<size[1]; j++){
                               for(int i=0; i<size[0]; i++){
                                       newImage.setRGB(j, size[0]-1-i, getColorAt(i, j).getRGB());
                               }
                       }
                       return newImage;
            default:
                return this.getImage();
        }
    }
    
    public BufferedImage getEdgeDetection(){
        int [] size = this.getSize();
        BufferedImage newImage = new BufferedImage((int)(size[0]), (int)(size[1]), BufferedImage.TYPE_INT_RGB);
        processed = true;
        
        for(int j=0; j<size[1]; j++){
            for(int i=0; i<size[0]; i++){
                try{
                    int[][] EDGE_MASK = new int[][]{new int[]{-1, 0, 1}, new int[]{-2, 0, 2}, new int[]{-1, 0, 1}};
                    double colorRed=0;
                    double colorGreen=0;
                    double colorBlue=0;
                    for(int row=0; row<3; row++){
                        for(int col=0; col<3; col++){
                            //X direction convolution process
                            colorRed = colorRed + ((double)(getColorAt(i-1+col, j-1+row).getRed()*EDGE_MASK[row][col]));
                            colorGreen = colorGreen + ((double)(getColorAt(i-1+col, j-1+row).getGreen()*EDGE_MASK[row][col]));
                            colorBlue = colorBlue + ((double)(getColorAt(i-1+col, j-1+row).getBlue()*EDGE_MASK[row][col]));

                            //Y direction convolution process
                            colorRed = colorRed + ((double)(getColorAt(i-1+col, j-1+row).getRed()*EDGE_MASK[2-col][row]));
                            colorGreen = colorGreen + ((double)(getColorAt(i-1+col, j-1+row).getGreen()*EDGE_MASK[2-col][row]));
                            colorBlue = colorBlue + ((double)(getColorAt(i-1+col, j-1+row).getBlue()*EDGE_MASK[2-col][row]));
                        }
                    }
                    newImage.setRGB(i, j, new Color((int)(colorRed), (int)colorGreen, (int)colorBlue).getRGB()); 
                }catch(ArrayIndexOutOfBoundsException e){}
                }
        }
        return newImage;
    }
    
    public BufferedImage getFliped(int axis){
        /* axis=0 => horixontal flip*/
        int [] size = this.getSize();
        BufferedImage newImage = new BufferedImage((int)(size[0]), (int)(size[1]), BufferedImage.TYPE_INT_RGB);
        processed = true;
        
        if(axis==0){
            for (int j=0; j<size[1]; j++){
                for(int i=0; i<size[0]; i++){
                    newImage.setRGB(i, size[1]-1-j, getColorAt(i, j).getRGB());
                }
            }
            return newImage;
        }else{
            for (int j=0; j<size[1]; j++){
                for(int i=0; i<size[0]; i++){
                    newImage.setRGB(size[0]-1-i, j, getColorAt(i, j).getRGB());
                }
            }
            return newImage;
        }
    }
    
   
    

    
    
    
}