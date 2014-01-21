package org.artofsolving.jodconverter.sample.web;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.office.OfficeException;

public class ConverterServlet extends HttpServlet {

    private static final long serialVersionUID = -591469426224201748L;

    private final Logger logger = Logger.getLogger(getClass().getName());

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!ServletFileUpload.isMultipartContent(request)) {
        	response.sendError(HttpServletResponse.SC_FORBIDDEN, "only multipart requests are allowed");
        	return;
        }

		WebappContext webappContext = WebappContext.get(getServletContext());
		ServletFileUpload fileUpload = webappContext.getFileUpload();
		OfficeDocumentConverter converter = webappContext.getDocumentConverter();

		String outputExtension = FilenameUtils.getExtension(request.getRequestURI());
		        
		FileItem uploadedFile;
		try {
			uploadedFile = getUploadedFile(fileUpload, request);
		} catch (FileUploadException fileUploadException) {
		    throw new ServletException(fileUploadException);
		}
		if (uploadedFile == null) {
			throw new NullPointerException("uploaded file is null");
		}
        String inputExtension = FilenameUtils.getExtension(uploadedFile.getName());

        String baseName = FilenameUtils.getBaseName(uploadedFile.getName());
        File inputFile = File.createTempFile(baseName, "." + inputExtension);
        writeUploadedFile(uploadedFile, inputFile);
        File outputFile = new File(System.getProperty("user.dir") + File.separator + "pdfout" + File.separator + baseName + "." + outputExtension);

        try {
            DocumentFormat outputFormat = converter.getFormatRegistry().getFormatByExtension(outputExtension);
        	long startTime = System.currentTimeMillis();
            if (inputExtension.equalsIgnoreCase("pdf")) {
                copyFile(inputFile,outputFile);
            } else {
        	    converter.convert(inputFile, outputFile);
            }
        	long conversionTime = System.currentTimeMillis() - startTime;
        	logger.info(String.format("successful conversion: %s [%db] to %s in %dms", inputExtension, inputFile.length(), outputExtension, conversionTime));

            //convert to HTML
            ShellExec exec = new ShellExec(false,false);
            //String[] cmd = { "pdf2htmlEX.exe",inputFile.getAbsolutePath()};
            //Process p = Runtime.getRuntime().exec(cmd);
            //int ret = p.waitFor();
            //pdf2htmlEX --embed cfijo --split-pages 1 --dest-dir out --page-filename test-%d.page pdf/test.pdf
            String dest = UUID.randomUUID().toString();
            exec.execute("pdf2htmlEX.exe",null,true,
                    //"--debug","1",
                    "--fallback","1",
                    "--split-pages","1",
                    "--embed","cfijo",
                    "--dest-dir", "htmlout/" + dest,
                    //"--page-filename","test-%d.page",
                    "pdfout/"  + baseName + ".pdf");
           // logger.info(exec.getError());
           // logger.info(exec.getOutput());

            logger.info(String.format("successful html: %s ", "htmlout/" + dest + "/" + baseName + ".html"));

            response.sendRedirect(response.encodeRedirectURL("/out/" + dest + "/"  + baseName + ".html"));
        	//response.setContentType(outputFormat.getMediaType());
            //response.setHeader("Content-Disposition", "attachment; filename=" + baseName + "." + outputExtension);
            //sendFile(outputFile, response);
        } catch (OfficeException oe) {
            logger.severe(String.format("failed conversion: %s [%db] to %s; %s; input file: %s", inputExtension, inputFile.length(), outputExtension, oe, inputFile.getName()));
            throw new ServletException("conversion failed. this format is not supported.");
        } catch (Exception exception) {
            logger.severe(String.format("failed conversion: %s [%db] to %s; %s; input file: %s", inputExtension, inputFile.length(), outputExtension, exception, inputFile.getName()));
        	throw new ServletException("conversion failed", exception);
        } finally {
        	outputFile.delete();
        	inputFile.delete();
        }
	}

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

	private void sendFile(File file, HttpServletResponse response) throws IOException {
		response.setContentLength((int) file.length());
        InputStream inputStream = null;
        try {
        	inputStream = new FileInputStream(file);
            IOUtils.copy(inputStream, response.getOutputStream());
        } finally {
        	IOUtils.closeQuietly(inputStream);
        }
	}

	private void writeUploadedFile(FileItem uploadedFile, File destinationFile) throws ServletException {
        try {
			uploadedFile.write(destinationFile);
		} catch (Exception exception) {
			throw new ServletException("error writing uploaded file", exception);
		}
		uploadedFile.delete();
	}

	private FileItem getUploadedFile(ServletFileUpload fileUpload, HttpServletRequest request) throws FileUploadException {
		@SuppressWarnings("unchecked")
		List<FileItem> fileItems = fileUpload.parseRequest(request);
		for (FileItem fileItem : fileItems) {
			if (!fileItem.isFormField()) {
				return fileItem;
			}
		}
		return null;
	}

}
