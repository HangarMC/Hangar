package io.papermc.hangar.components.images.service;

import com.luciad.imageio.webp.WebPWriteParam;
import io.papermc.hangar.HangarComponent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ImageService extends HangarComponent {

    private static final MediaType WEBP = new MediaType("image", "webp");

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    public byte[] convertAndOptimize(final byte[] imageBytes) throws IOException {
        final InputStream in = new ByteArrayInputStream(imageBytes);
        BufferedImage bufferedImage = ImageIO.read(in);

        // scale
        bufferedImage = this.scale(bufferedImage, this.config.image.size(), this.config.image.size());

        // convert to webp and optimize
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(WEBP.toString());
        if (writers.hasNext()) {
            final ImageWriter writer = writers.next();
            final ImageWriteParam params = new WebPWriteParam(writer.getLocale());
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionType(params.getCompressionTypes()[WebPWriteParam.LOSSY_COMPRESSION]);
            params.setCompressionQuality(this.config.image.quality());

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ImageOutputStream ios = ImageIO.createImageOutputStream(out);
            writer.setOutput(ios);

            try {
                final IIOImage optimizedImage = new IIOImage(bufferedImage, null, null);
                writer.write(null, optimizedImage, params);

                writer.dispose();
                ios.flush();

                return out.toByteArray();
            } catch (final IIOException ex) {
                LOGGER.warn("Error while converting to webp!", ex);
                return imageBytes;
            }
        } else {
            LOGGER.warn("No webp writer found!");
            return imageBytes;
        }
    }

    private BufferedImage scale(final BufferedImage originalImage, final int height, final int width) {
        final AffineTransform af = new AffineTransform();
        af.scale((double) width / originalImage.getWidth(), (double) height / originalImage.getHeight());
        final AffineTransformOp operation = new AffineTransformOp(af, AffineTransformOp.TYPE_BILINEAR);

        BufferedImage rescaledImage = new BufferedImage(width, height, originalImage.getType());
        rescaledImage = operation.filter(originalImage, rescaledImage);

        return rescaledImage;
    }
}
