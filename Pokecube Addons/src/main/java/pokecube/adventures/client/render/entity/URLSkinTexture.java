package pokecube.adventures.client.render.entity;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** This class is directly based on the URL Skin texture used by CustomNPCs. */
@SideOnly(Side.CLIENT)
public class URLSkinTexture extends SimpleTexture
{
    private static final Logger        logger                = LogManager.getLogger();
    private static final AtomicInteger threadDownloadCounter = new AtomicInteger(0);
    private final File                 cacheFile;
    private final String               imageUrl;
    private final IImageBuffer         imageBuffer;
    private BufferedImage              bufferedImage;
    private Thread                     imageThread;
    private boolean                    textureUploaded;

    public URLSkinTexture(File file, String url, ResourceLocation resource, IImageBuffer buffer)
    {
        super(resource);
        this.cacheFile = file;
        this.imageUrl = url;
        this.imageBuffer = buffer;
    }

    private void checkTextureUploaded()
    {
        if (!this.textureUploaded)
        {
            if (this.bufferedImage != null)
            {
                if (this.textureLocation != null)
                {
                    this.deleteGlTexture();
                }

                TextureUtil.uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
                this.textureUploaded = true;
            }
        }
    }

    @Override
    public int getGlTextureId()
    {
        this.checkTextureUploaded();
        return super.getGlTextureId();
    }

    public void setBufferedImage(BufferedImage image)
    {
        this.bufferedImage = image;

        if (this.imageBuffer != null)
        {
            this.imageBuffer.skinAvailable();
        }
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException
    {
        if (this.bufferedImage == null && this.textureLocation != null)
        {
            super.loadTexture(resourceManager);
        }

        if (this.imageThread == null)
        {
            if (this.cacheFile != null && this.cacheFile.isFile())
            {
                logger.debug("Loading http texture from local cache ({})", new Object[] { this.cacheFile });

                try
                {
                    this.bufferedImage = ImageIO.read(this.cacheFile);

                    if (this.imageBuffer != null)
                    {
                        this.setBufferedImage(this.imageBuffer.parseUserSkin(this.bufferedImage));
                    }
                }
                catch (IOException ioexception)
                {
                    logger.error("Couldn\'t load skin " + this.cacheFile, ioexception);
                    this.loadTextureFromServer();
                }
            }
            else
            {
                this.loadTextureFromServer();
            }
        }
    }

    protected void loadTextureFromServer()
    {
        this.imageThread = new Thread("Texture Downloader #" + threadDownloadCounter.incrementAndGet())
        {
            @Override
            public void run()
            {
                HttpURLConnection connection = null;
                URLSkinTexture.logger.debug("Downloading http texture from {} to {}",
                        new Object[] { URLSkinTexture.this.imageUrl, URLSkinTexture.this.cacheFile });

                try
                {
                    connection = (HttpURLConnection) (new URL(URLSkinTexture.this.imageUrl))
                            .openConnection(Minecraft.getMinecraft().getProxy());
                    connection.setDoInput(true);
                    connection.setDoOutput(false);
                    connection.setRequestProperty("User-Agent",
                            "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0");
                    connection.connect();

                    if (connection.getResponseCode() / 100 != 2) { return; }

                    BufferedImage bufferedimage;

                    if (URLSkinTexture.this.cacheFile != null)
                    {
                        FileUtils.copyInputStreamToFile(connection.getInputStream(), URLSkinTexture.this.cacheFile);
                        bufferedimage = ImageIO.read(URLSkinTexture.this.cacheFile);
                    }
                    else
                    {
                        bufferedimage = TextureUtil.readBufferedImage(connection.getInputStream());
                    }

                    if (URLSkinTexture.this.imageBuffer != null)
                    {
                        bufferedimage = URLSkinTexture.this.imageBuffer.parseUserSkin(bufferedimage);
                    }

                    URLSkinTexture.this.setBufferedImage(bufferedimage);
                }
                catch (Exception exception)
                {
                    URLSkinTexture.logger.error("Couldn\'t download http texture", exception);
                }
                finally
                {
                    if (connection != null)
                    {
                        connection.disconnect();
                    }
                }
            }
        };
        this.imageThread.setDaemon(true);
        this.imageThread.start();
    }
}