package com.quashbugs.quash.service;

import com.github.jaiimageio.impl.plugins.gif.GIFImageWriter;
import com.github.jaiimageio.impl.plugins.gif.GIFImageWriterSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

@Service
public class GifCreationService {

    private static final Logger logger = LoggerFactory.getLogger(GifCreationService.class);

    public ByteArrayOutputStream createGif(List<MultipartFile> files, int delayTime) throws IOException {
        logger.info("Creating GIF with {} bitmaps and delay time: {}", files.size(), delayTime);

        List<BufferedImage> images = files.parallelStream()
                .map(file -> {
                    try {
                        return ImageIO.read(file.getInputStream());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .toList();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageWriter writer = new GIFImageWriter(new GIFImageWriterSpi());
        IIOMetadata metadata = getMetadata(writer, delayTime);

        try (ImageOutputStream output = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(output);
            writer.prepareWriteSequence(null);

            int batchSize = 10;
            int totalImages = images.size();
            int processedImages = 0;

            logger.info("Starting GIF generation from {} images with batch size {}", totalImages, batchSize);

            while (processedImages < totalImages) {
                int batchEnd = Math.min(processedImages + batchSize, totalImages);
                List<BufferedImage> batch = images.subList(processedImages, batchEnd);

                logger.debug("Processing batch of {} images (index {} to {})", batch.size(), processedImages, batchEnd - 1);

                for (int i = 0; i < batch.size(); i++) {
                    BufferedImage img = batch.get(i);
                    try {
                        logger.debug("Writing bitmap {} to GIF sequence inside createGif", processedImages + i);
                        writer.writeToSequence(new javax.imageio.IIOImage(img, null, metadata), null);
                    } catch (javax.imageio.IIOException e) {
                        logger.error("An error occurred while processing the GIF generation request for bitmap inside createGif {}: {}", processedImages + i, e.getMessage(), e);
                        throw new IOException("An error occurred while processing the GIF generation request: " + e.getMessage(), e);
                    }
                }

                processedImages = batchEnd;
                logger.debug("Processed {} out of {} images", processedImages, totalImages);
            }

            writer.endWriteSequence();
            logger.info("GIF generation completed successfully");

        } catch (IOException e) {
            logger.error("An error occurred while generating the GIF inside createGif: {}", e.getMessage(), e);
            throw new IOException("An error occurred while generating the GIF inside createGif: " + e.getMessage(), e);
        } finally {
            writer.dispose();
            writer = null;
            metadata = null;
            System.gc();
        }

        logger.info("GIF created successfully inside createGif");
        return outputStream;
    }

    public ByteArrayOutputStream createGifFromByteArrays(List<byte[]> imageByteArrays, int delayTime) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageWriter writer = new GIFImageWriter(new GIFImageWriterSpi());
        IIOMetadata metadata = getMetadata(writer, delayTime);

        try (ImageOutputStream output = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(output);
            writer.prepareWriteSequence(null);

            int batchSize = 10;
            int totalImages = imageByteArrays.size();
            int processedImages = 0;

            logger.info("Starting GIF generation from {} images with batch size {}", totalImages, batchSize);

            while (processedImages < totalImages) {
                int batchEnd = Math.min(processedImages + batchSize, totalImages);
                List<byte[]> batch = imageByteArrays.subList(processedImages, batchEnd);

                logger.debug("Processing batch of {} images (index {} to {})", batch.size(), processedImages, batchEnd - 1);

                for (byte[] imageBytes : batch) {
                    try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
                        BufferedImage image = ImageIO.read(inputStream);
                        if (image == null) {
                            String errorMessage = "Invalid image format";
                            logger.error(errorMessage);
                            throw new IOException(errorMessage);
                        }
                        writer.writeToSequence(new IIOImage(image, null, metadata), null);
                    } catch (IOException e) {
                        String errorMessage = "An error occurred while processing the image";
                        logger.error(errorMessage, e);
                        throw e;
                    }
                }

                processedImages = batchEnd;
                logger.debug("Processed {} out of {} images", processedImages, totalImages);
            }

            writer.endWriteSequence();
            logger.info("GIF generation completed successfully");
        } catch (IOException e) {
            String errorMessage = "An error occurred while generating the GIF";
            logger.error(errorMessage, e);
            throw e;
        } finally {
            writer.dispose();
            writer = null;
            metadata = null;
            System.gc();
        }

        return outputStream;
    }

    private IIOMetadata getMetadata(ImageWriter writer, int delayTime) throws IIOInvalidTreeException {
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);

        IIOMetadata metadata = writer.getDefaultImageMetadata(imageTypeSpecifier, writeParam);
        String metaFormatName = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delayTime));
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");
        child.setUserObject(new byte[]{0x1, 0x0, 0x0});
        appExtensionsNode.appendChild(child);

        metadata.setFromTree(metaFormatName, root);
        return metadata;
    }

    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }
}