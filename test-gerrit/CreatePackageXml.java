import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
//git -gerrit
public class CreatePackageXml {

	private static HashMap<String, List<String>> refinedFiles = new HashMap<String, List<String>>();
	private static HashMap<String, List<String>> oRefinedFiles = new HashMap<String, List<String>>();
	private static HashMap<String, String> xmlFileToDeployFolderNameMap;
	static {
		xmlFileToDeployFolderNameMap = new HashMap<String, String>();
		xmlFileToDeployFolderNameMap.put("ApexClass", "classes");
		xmlFileToDeployFolderNameMap.put("ApexPage", "pages");
	}

	public static void main(String[] args) {
		String filename = "C:/View/SalesForce code/wcis/sfdcint/sfdc/SaurabhDevOrg/git.txt";
		List<String> changedFiles = readFile(filename);
		refinedFiles = refineChangedFiles(changedFiles);
		createPkgXml(refinedFiles);
		modifyFiles(oRefinedFiles);
	}

	private static List<String> readFile(String filename) {
		List<String> changedFiles = new ArrayList<String>();
		File file = new File(filename);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			System.out.println("************* Checked in Files *************");
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				int index = line.lastIndexOf("/");
				String fileName = line.substring(index + 1);
				if (!fileName.isEmpty()) {
					System.out.println(fileName);
					changedFiles.add(fileName);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return changedFiles;
	}

	private static void createPkgXml(HashMap<String, List<String>> changedFiles) {
		try {
			// Creating an empty XML Document
			// We need a Document
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// Creating the XML tree
			// create the root element and add it to the document
			Element root = doc.createElementNS("http://soap.sforce.com/2006/04/metadata", "Package");
			doc.appendChild(root);

			// Iterator it = changedFiles.entrySet().iterator();
			for (Entry<String, List<String>> entry : changedFiles.entrySet()) {
				// types node
				Element types = doc.createElement("types");
				root.appendChild(types);
				// name node
				Element name = doc.createElement("name");
				types.appendChild(name);
				String componentType = entry.getKey();
				Text text = doc.createTextNode(componentType);
				name.appendChild(text);
				// System.out.println(componentType);
				List<String> files = changedFiles.get(componentType);
				for (String fileName : files) {
					Element members = doc.createElement("members");
					types.appendChild(members);
					Text file = doc.createTextNode(fileName);
					members.appendChild(file);
				}
			}
			Element version = doc.createElement("version");
			root.appendChild(version);
			Text text = doc.createTextNode("31.0");
			version.appendChild(text);

			// Output the XML

			// set up a transformerj
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			// create string from xml tree
			// StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(new File(
					"C:/View/SalesForce code/wcis/sfdcint/sfdc/SaurabhDevOrg/ant-deploy/Saurabh_DevOrg/src/package.xml"));
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			// String xmlString = sw.toString();
			System.out.println("************* Package.xml *************");
			// print xml
			// System.out.println(xmlString);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static HashMap<String, List<String>> refineChangedFiles(List<String> changedFiles) {

		HashMap<String, List<String>> refinedFiles = new HashMap<String, List<String>>();
		HashMap<String, List<String>> origRefinedFiles = new HashMap<String, List<String>>();

		List<String> list_ApexClass = new ArrayList<String>();
		List<String> list_ApexTrigger = new ArrayList<String>();
		List<String> list_ApexPage = new ArrayList<String>();

		List<String> flist_ApexClass = new ArrayList<String>();
		List<String> flist_ApexTrigger = new ArrayList<String>();
		List<String> flist_ApexPage = new ArrayList<String>();

		for (String file : changedFiles) {
			if (file.contains(".cls")) {
				flist_ApexClass.add(file);
				String tempFile = file.replace(".cls", "");
				list_ApexClass.add(tempFile);
			} else if (file.contains(".page")) {
				flist_ApexPage.add(file);
				String tempFile = file.replace(".page", "");
				list_ApexPage.add(tempFile);
			}
			// keep adding here for different component
		}

		if (list_ApexClass.size() > 0) {
			refinedFiles.put("ApexClass", list_ApexClass);
			origRefinedFiles.put("ApexClass", flist_ApexClass);
		}
		if (list_ApexPage.size() > 0) {
			refinedFiles.put("ApexPage", list_ApexPage);
			origRefinedFiles.put("ApexPage", flist_ApexPage);
		}
		oRefinedFiles = origRefinedFiles;

		return refinedFiles;
	}

	public static void createFolderStructure(HashMap<String, List<String>> changedFiles) {
		try {
			File sourceLocation = new File(
					"C:/View/SalesForce code/wcis/sfdcint/sfdc/SaurabhDevOrg/ant-deploy/Saurabh_DevOrg/src");
			File targetLocation = new File("C:/View/SalesForce code/wcis/sfdcint/sfdc/SaurabhDevOrg/deploy/");
			if (sourceLocation.isDirectory()) {
				if (!targetLocation.exists()) {
					targetLocation.mkdir();
				}

				for (Entry<String, List<String>> entry : changedFiles.entrySet()) {
					String fileType = entry.getKey();
					List<String> fileName = entry.getValue();
					File targetLocation1 = new File(
							"C:/View/SalesForce code/wcis/sfdcint/sfdc/SaurabhDevOrg/deploy/" + fileType);

					File sourceLocation1 = new File(
							"C:/View/SalesForce code/wcis/sfdcint/sfdc/SaurabhDevOrg/ant-deploy/Saurabh_DevOrg/src/"
									+ xmlFileToDeployFolderNameMap.get(fileType));
					File[] files = sourceLocation1.listFiles();
					for (File file : files) {
						InputStream in = new FileInputStream(file);
						File destFile = new File(targetLocation1 + fileType + "/" + file.getName());
						if (!destFile.exists()) {
							destFile.createNewFile();
						}
						OutputStream out = new FileOutputStream(destFile);

						for (String name : fileName) {
							if (file.getName().equals(name)) {
								// Files.copy(sourceLocation1, target, options);

								// Copy the bits from input stream to output
								// stream
								byte[] buf = new byte[1024];
								int len;
								while ((len = in.read(buf)) > 0) {
									out.write(buf, 0, len);
								}
								in.close();
								out.close();

							}
						}

					}

				}
			}

		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}

	public static void modifyFiles(HashMap<String, List<String>> changedFiles) {
		String sourceDir = "C:/View/SalesForce code/wcis/sfdcint/sfdc/SaurabhDevOrg/ant-deploy/Saurabh_DevOrg/src";
		File sourceDirLocation = new File(sourceDir);
		// returns pathnames for files and directory
		File[] paths = sourceDirLocation.listFiles();
		Boolean delete = true;
		// for each pathname in pathname array
		for (File path : paths) {
			delete = true;
			for (String otherFileOrDir : changedFiles.keySet()) {
				if ((path.getName().equals(xmlFileToDeployFolderNameMap.get(otherFileOrDir)))) {
					delete = false;
					break;
				}
			}
			if (delete) {
				File f1 = new File(sourceDir + "/" + path.getName());
				System.out.println("Deleting folder : "+f1.getName());
				File[] f2 = f1.listFiles();
				if (f2 != null && f2.length > 0) {
					for (File f3 : f2) {
						try{							
							f3.delete();
							if(f1.getName().equals("email")){
								File[] f4 = f3.listFiles();
								for(File f5 : f4){
									System.out.println("Del : -- "+f5.getName());
									f5.delete();
								}
								f3.delete();
							}
						}catch(Exception ex){
							System.out.println(ex);
							File[] f4 = f3.listFiles();
							for(File f5 : f4){
								System.out.println("Del : -- "+f5.getName());
								f5.delete();
							}
							f3.delete();
						}
						
					}
					f1.delete();					
				}
			}

		}

		for (Entry<String, List<String>> entry : changedFiles.entrySet()) {
			String fileType = entry.getKey();
			List<String> fileName = entry.getValue();
			File sourceLocation = new File(
					"C:/View/SalesForce code/wcis/sfdcint/sfdc/SaurabhDevOrg/ant-deploy/Saurabh_DevOrg/src/"
							+ xmlFileToDeployFolderNameMap.get(fileType));
			File[] files = sourceLocation.listFiles();
			for (File file : files) {
				delete = true;
				for (String name : fileName) {
					if ((file.getName().equals(name) || ((file.getName()).equals(name + "-meta.xml")))) {
						delete = false;
						break;
					}
				}
				if(delete){
					file.delete();
					System.out.println("Deleting : "+file.getName());
				}
			}
		}
	}
}
