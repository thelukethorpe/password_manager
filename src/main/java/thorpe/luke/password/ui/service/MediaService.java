package thorpe.luke.password.ui.service;

import java.io.File;
import java.net.MalformedURLException;
import javafx.scene.image.Image;

public class MediaService {

  private final String pathToAssets;

  public MediaService(String pathToAssets) {
    this.pathToAssets = pathToAssets;
  }

  private String getNormalizedPathFromFilename(String filename) {
    String pathToFile = pathToAssets + filename;
    File file = new File(pathToFile);
    try {
      return file.toURI().toURL().toString();
    } catch (MalformedURLException e) {
      throw new RuntimeException("Failed to parse media at \"" + pathToFile + ".\"");
    }
  }

  public Image loadImage(String filename) {
    return new Image(getNormalizedPathFromFilename(filename));
  }
}
