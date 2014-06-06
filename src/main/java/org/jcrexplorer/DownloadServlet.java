package org.jcrexplorer;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This Servlet is used for download files.
 */
public class DownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private Log logger = LogFactory.getLog(this.getClass());

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {

		HttpSession httpSession = request.getSession(false);
		if (httpSession == null) {
			logger.warn("No active HttpSession found, binary value cannot be displayed. Is that call really from the explorer app?");
			return;
		}

		Object o = httpSession.getAttribute("ContentBean");
		if (o == null) {
			logger.warn("ContentBean could not be found in HttpSession, so JCR session cannot be retrieved. Binary value can't be displayed.");
			return;
		}

		if (!(o instanceof ContentBean)) {
			logger.warn("ContentBean object bound in HttpSession is not of class org.jcrexplorer.ContentBean, so JCR session cannot be retrieved. Binary value can't be displayed.");
			return;
		}

		ContentBean contentBean = (ContentBean) o;
		Session session = contentBean.getSession();

		if (session == null) {
			logger.warn("JCR session cannot be retrieved. Binary value can't be displayed.");
			return;
		}

		response.reset();
		response.setContentType("application/x-gzip"); // gzipped text/xml
		response.setHeader("Content-Disposition", "attachment;filename=export.xml.gz");
		ServletOutputStream out = null;
		GZIPOutputStream zout = null;
		
		try {
			out = response.getOutputStream();
			zout = new GZIPOutputStream(out);
			// skipBinary=false , noRecurse: false
			contentBean.getSession().exportSystemView(contentBean.getCurrentNode().getNode().getPath(), zout, false, false);
		} catch (IOException err) {
			err.printStackTrace();
		} catch (PathNotFoundException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} finally {
			try {
				if (zout != null) {
					zout.close();
				}
			} catch (IOException err) {
				err.printStackTrace();
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException err) {
				err.printStackTrace();
			}
		}
	}

}
