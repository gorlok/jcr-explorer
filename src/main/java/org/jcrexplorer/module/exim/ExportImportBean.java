package org.jcrexplorer.module.exim;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.util.zip.GZIPInputStream;

import javax.faces.model.SelectItem;
import javax.jcr.ImportUUIDBehavior;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.jcrexplorer.Constants;
import org.jcrexplorer.module.ModuleBean;

/**
 * This class handles Export and Import.
 * 
 * @author Beck
 * 
 */
public class ExportImportBean extends ModuleBean {

	private static final int EXPORT_SYSTEM_VIEW = 0;

	private static final int EXPORT_DOCUMENT_VIEW = 1;

	private boolean exportBinaries = false;

	private boolean exportRecursive = true;

	private int importUUIDBehavior;

	private String exportResults;

	private String importData;

	private int exportView = EXPORT_SYSTEM_VIEW;
	
	private UploadedFile uploadedFile;
	
	public String exportData() {
		OutputStream out = new ByteArrayOutputStream();
		try {
			if (exportView == EXPORT_SYSTEM_VIEW) {
				contentBean.getSession().exportSystemView(
						contentBean.getCurrentNode().getNode().getPath(), out,
						!exportBinaries, !exportRecursive);
			} else {
				contentBean.getSession().exportDocumentView(
						contentBean.getCurrentNode().getNode().getPath(), out,
						!exportBinaries, !exportRecursive);
			}
		} catch (Exception ex) {
			addErrorMessage("An " + ex.getClass().getName()
					+ " occured while exporting data: " + ex.getMessage());
			return Constants.OUTCOME_FAILURE;
		}
		exportResults = out.toString();
		addInfoMessage("Data export successful.");
		return "goToExportResults";
	}
	
	public String getExportResults() {
		return exportResults;
	}

	public String getImportData() {
		return importData;
	}

	public String importData() {
		if ((importData == null) || ("".equals(importData))) {
			addErrorMessage("No data to import!");
			return Constants.OUTCOME_FAILURE;
		}
		InputStream in = new StringBufferInputStream(importData);
		try {
			contentBean.getSession().getWorkspace().importXML(
					contentBean.getCurrentNode().getNode().getPath(), in,
					importUUIDBehavior);
		} catch (Exception ex) {
			addErrorMessage("An " + ex.getClass().getName()
					+ " occured while importing data: " + ex.getMessage());
			return Constants.OUTCOME_FAILURE;
		}
		importData = "";
		addInfoMessage("Data import successful.");
		contentBean.refresh();
		return Constants.OUTCOME_SUCCESS;
	}
	
	public UploadedFile getFileupload() {
		return null;
	}
	
	private File tempFile = null;
	
	/**
	 * @todo Move to ValueWrapper
	 * @param file
	 */
	public void setFileupload(UploadedFile file) {

		InputStream is = null;
		FileOutputStream fos = null;
		try {
			// if no new binary is posted its nothing to do here
			if (file == null)
				return;

			tempFile = File.createTempFile("import", "xml.gz");
			is = (file.getInputStream());
			fos = new FileOutputStream(tempFile);

			byte[] buf = new byte[1024];
			int len;
			while ((len = is.read(buf)) > 0) {
				fos.write(buf, 0, len);
			}	
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) try { is.close(); } catch (Exception e) {}
			if (fos != null) try { fos.close(); } catch (Exception e) {}
		}
	}

	public String importDataFromFile() {
		if ((tempFile == null)) {
			addErrorMessage("No data to import!");
			return Constants.OUTCOME_FAILURE;
		}
		GZIPInputStream in = null;
		try {
			in = new GZIPInputStream(new FileInputStream(tempFile));
			contentBean.getSession().getWorkspace().importXML(
					contentBean.getCurrentNode().getNode().getPath(), in,
					importUUIDBehavior);
		} catch (Exception ex) {
			addErrorMessage("An " + ex.getClass().getName()
					+ " occured while importing data: " + ex.getMessage());
			return Constants.OUTCOME_FAILURE;
		} finally {
			if (in != null) try { in.close(); } catch (Exception e) {}
		}
		importData = "";
		tempFile = null;
		addInfoMessage("Data import successful.");
		contentBean.refresh();
		return Constants.OUTCOME_SUCCESS;
	}
	
	public boolean isExportBinaries() {
		return exportBinaries;
	}

	public boolean isExportRecursive() {
		return exportRecursive;
	}

	public void setExportBinaries(boolean exportBinaries) {
		this.exportBinaries = exportBinaries;
	}

	public void setExportRecursive(boolean exportRecursive) {
		this.exportRecursive = exportRecursive;
	}

	public void setExportResults(String exportResults) {
		this.exportResults = exportResults;
	}

	public void setImportData(String importData) {
		this.importData = importData;
	}

	public int getImportUUIDBehavior() {
		return importUUIDBehavior;
	}

	public void setImportUUIDBehavior(int inportUUIDBehavior) {
		this.importUUIDBehavior = inportUUIDBehavior;
	}

	public SelectItem[] getImportUUIDBehaviorList() {
		SelectItem s1 = new SelectItem(
				ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING,
				"Remove existing UUIDs");
		SelectItem s2 = new SelectItem(
				ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING,
				"Replace existing UUIDs");
		SelectItem s3 = new SelectItem(
				ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW,
				"Throw error on UUID collision");
		SelectItem s4 = new SelectItem(
				ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW,
				"Create new UUIDs for all nodes");
		return new SelectItem[] { s1, s2, s3, s4 };
	}

	public SelectItem[] getExportViewList() {
		SelectItem s1 = new SelectItem(EXPORT_SYSTEM_VIEW, "Export System View");
		SelectItem s2 = new SelectItem(EXPORT_DOCUMENT_VIEW,
				"Export Document View");
		return new SelectItem[] { s1, s2 };
	}

	public int getExportView() {
		return exportView;
	}

	public void setExportView(int exportView) {
		this.exportView = exportView;
	}

}
