package net.codepixl.GLCraft.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.JOptionPane;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Main extends Application {
    private Scene scene;
    private String installedVersion = "A really old version";
    private String readableInstalledVersion = "None";
    private final BorderPane layout = new BorderPane();
    private final Button launch = new Button("Launch GLCraft");
    private final Text lbl = new Text("Loading...");
    private boolean doneDownloadProcess = false;
    
    public static boolean needsBootstrapUpdate = true;
    public static final String currentBootstrapVer = "1";
    
    Download d;
    @Override public void start(Stage stage) throws IOException {
    	//JOptionPane.showMessageDialog(null, "The GLCraft Launcher will open once you click OK. This may take a little bit while it determines the latest version.\n\nClick OK to continue.", "GLCraft Launcher", JOptionPane.INFORMATION_MESSAGE, null);
        // create the scene
    	if(needsBootstrapUpdate){
    		JOptionPane.showMessageDialog(null, "You are launching GLCraft with an older launcher that needs to be manually updated.\nPlease go to http://www.codepixl.net/GLCraft/ to download a new launcher.", "Warning", JOptionPane.WARNING_MESSAGE);
    	}
        installedVersion = getVersion();
        if(installedVersion != "0"){
        	readableInstalledVersion = installedVersion.substring(0, 3)+"."+installedVersion.substring(3, 4);
        }
        Browser b = new Browser();
        Pane bottomPane = createSidebarContent();
        Pane mainPane = VBoxBuilder.create().spacing(10)
        .children(
        b
        ).build();
        layout.setCenter(mainPane);
        layout.setBottom(bottomPane);
        stage.setTitle("GLCraft Launcher");
        scene = new Scene(layout);
        stage.setScene(scene);
        stage.getIcons().add(new Image("file:res/icon32.png"));
        stage.setMinWidth(1000);
        stage.setWidth(1000);
        stage.setResizable(false);
        System.out.println("shows");
        stage.show();
        System.out.println("show");
        Timeline t = new Timeline(new KeyFrame(Duration.ZERO, new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				if(d != null && d.getProgress() == 100 && !doneDownloadProcess){
					update("donedownloading");
				}
			}
        	
        }), new KeyFrame(Duration.millis(1)));
        t.setCycleCount(Timeline.INDEFINITE);
        t.play();
    }
    
    public void update(String stat){
    	if(stat == "donedownloading" && !doneDownloadProcess){
    		File downloadZip = new File(System.getProperty("user.home")+"\\GLCraft\\GLCraft\\GLCraft.zip");
    		System.out.println("Done Downloading, Unzipping");
            unzip(downloadZip.toString(),System.getProperty("user.home")+"\\GLCraft","nil");
            System.out.println("Unzipped");
            FileUtils.deleteQuietly(downloadZip);
            download("http://www.codepixl.net/GLCraft/ver.txt", new File(System.getProperty("user.home")+"\\GLCraft\\ver.txt"));
            try {
				installedVersion = getVersion();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if(installedVersion != "0"){
            	readableInstalledVersion = installedVersion.substring(0, 3)+"."+installedVersion.substring(3, 4);
            }
            lbl.setText("Installed Version: "+readableInstalledVersion);
            doneDownloadProcess = true;
            this.launch.setDisable(false);
            this.launch.setText("Launch GLCraft");
            JOptionPane.showMessageDialog(null, "Done downloading!","GLCraft Update", JOptionPane.INFORMATION_MESSAGE);
    	}
    }
    
    public static void main(String[] args){
    	for(String s : args){
    		if(s.contains("bootstrapver")){
    			if(s.substring(s.indexOf(":")+1).equalsIgnoreCase(currentBootstrapVer)){
    				needsBootstrapUpdate = false;
    			}
    		}
    	}
        launch(args);
    }
    private BorderPane createSidebarContent() throws NumberFormatException, IOException {
    	final ProgressBar pb = new ProgressBar(0);
    	final ProgressIndicator pi = new ProgressIndicator();
    	pi.setScaleX(0.5);
    	pi.setScaleY(0.5);
    	pi.setVisible(false);
    	pb.setVisible(false);
        lbl.setText("Installed Version: "+readableInstalledVersion);
        if(!hasInetConnection()){
        	lbl.setText(lbl.getText()+" (Offline)");
        }
        launch.setOnAction(new EventHandler<ActionEvent>(){
            @Override public void handle(ActionEvent actionEvent){
                if(launch.getText() == "Launch GLCraft"){
                	String curV = getCurVersion();
                    if(Double.parseDouble(curV) > Double.parseDouble(installedVersion)){
                        System.out.println(curV + ">" + installedVersion);
                        JOptionPane.showMessageDialog(null, "There is a new version of GLCraft ("+curV.substring(0, 3)+"."+curV.substring(3, 4)+"). It will install when you click the 'Update GLCraft' Button.", "GLCraft Update", JOptionPane.INFORMATION_MESSAGE);
                        launch.setText("Update GLCraft");
                        return;
                    }
	                System.out.println("launching: "+"java -Djava.library.path="+System.getProperty("user.home")+"\\GLCraft\\GLCraft"+" -jar "+System.getProperty("user.home")+"\\GLCraft\\GLCraft\\GLCraft.jar");
	                ProcessBuilder pb = new ProcessBuilder("java", "-Djava.library.path="+System.getProperty("user.home")+"\\GLCraft\\GLCraft","-jar", System.getProperty("user.home")+"\\GLCraft\\GLCraft\\GLCraft.jar");
	                try {
	                	pb.inheritIO();
	                	pb.environment().put("java.library.path",new File(System.getProperty("user.home")+"\\GLCraft\\GLCraft").getAbsolutePath());
	                    Process p = pb.start();
	                    System.exit(0);
	                }catch (IOException e) {
	                	JOptionPane.showMessageDialog(null, "Error","There was an error opening GLCraft. Maybe it's not installed properly?",JOptionPane.ERROR_MESSAGE);
	                    e.printStackTrace();
	                }
                }else if(launch.getText() == "Update GLCraft"){
                    System.out.println("update");
                    //JOptionPane.showMessageDialog(null, "Press OK to start downloading. Please wait while GLCraft downloads.", "GLCraft Update", JOptionPane.INFORMATION_MESSAGE);
                    FileUtils.deleteQuietly(new File(System.getProperty("user.home")+"\\GLCraft\\GLCraft"));
                	d = new Download("http://www.codepixl.net/downloads/GLCraft.zip", System.getProperty("user.home")+"/GLCraft/GLCraft/GLCraft.zip", pb, pi);
                	launch.setDisable(true);
                }
            }
        });
        final BorderPane launchPane = new BorderPane();
        final BorderPane progPane = new BorderPane();
        launchPane.setTop(progPane);
        launchPane.setCenter(lbl);
        launchPane.setBottom(launch);
        progPane.setCenter(pb);
        progPane.setRight(pi);
        progPane.setPrefHeight(10);
        launchPane.setStyle("-fx-background-color: lightgray;");
        progPane.setStyle("-fx-background-color: lightgray;");
        pb.prefWidthProperty().bind(launchPane.widthProperty().subtract(20));
        BorderPane.setAlignment(launch, Pos.CENTER);
        BorderPane.setAlignment(pb, Pos.CENTER);
        return launchPane;
    }
    private void download(String src, File dest){
		URL url;
		try {
			url = new URL(src);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", "CrapExplorer/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
			conn.connect();
			FileUtils.copyInputStreamToFile(conn.getInputStream(), dest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private String getCurVersion(){
    	URL url;
		try {
			if(hasInetConnection()){
				url = new URL("http://www.codepixl.net/GLCraft/ver.txt");
				URLConnection con = url.openConnection();
		        con.addRequestProperty("User-Agent", "CrapExplorer/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
		        con.setConnectTimeout(1000);
		        InputStream in = con.getInputStream();
		        String encoding = con.getContentEncoding();
		        encoding = encoding == null ? "UTF-8" : encoding;
		        String body = IOUtils.toString(in, encoding);
		        return body;
			}else{
				return "0";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "0";
		}
    }
    private void unzip(String source, String destination, String password){
        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }
            zipFile.extractAll(destination);
            } catch (ZipException e) {
            e.printStackTrace();
        }
    }
    private String getVersion() throws IOException{
        return readFile(System.getProperty("user.home")+"\\GLCraft\\ver.txt",Charset.forName("UTF8"));
    }
    static String readFile(String path, Charset encoding){
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
            } catch (IOException e) {
            return "0";
        }
    }
    private boolean hasInetConnection(){
    	Socket socket = null;
    	boolean reachable = false;
    	try {
    		InetAddress address = InetAddress.getByName(new URL("http://www.codepixl.net").getHost());
    	    socket = new Socket(address, 80);
    	    reachable = true;
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("network unreachable");
		} finally {            
    	    if (socket != null) try { socket.close(); } catch(IOException e) {}
    	}
    	return reachable;
    }
}
class Browser extends Region {
    
    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();
    
    public Browser() {
        //apply the styles
        getStyleClass().add("browser");
        // load the web page
        webEngine.load("http://www.codepixl.net/GLCraft/newsfeed.php");
        //add the web view to the scene
        getChildren().add(browser);
    }
    
    @Override protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
    }
    
    @Override protected double computePrefWidth(double height) {
        return 750;
    }
    
    @Override protected double computePrefHeight(double width) {
        return 500;
    }
}