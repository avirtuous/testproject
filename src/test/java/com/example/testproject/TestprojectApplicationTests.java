package com.example.testproject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@SpringBootTest
class TestprojectApplicationTests {

	List<String> filesListInDir = new ArrayList<>();

	@Test
	void compressor() {
		File file = new File("/Users/dongin-kim/test/README.md");
		String zipFileName = "/Users/dongin-kim/test/README.zip";

		File dir = new File("/Users/dongin-kim/test/zipfolder");
		String zipDirName = "/Users/dongin-kim/test/zipfolder/test.zip";
		try{
			zipSingleFile(file, zipFileName);
		} catch(Exception e){
			e.getLocalizedMessage();
		}
		try{
			TestprojectApplicationTests testprojectApplicationTests = new TestprojectApplicationTests();
			testprojectApplicationTests.makeFolderToZip(dir, zipDirName);
		} catch(Exception e){
			e.getLocalizedMessage();
		}

		}

	@Test
	void unZipTest(){
		Path source = Paths.get("/Users/dongin-kim/test/zipfolder/test.zip");
		Path target = Paths.get("/Users/dongin-kim/test/zipfolder/unzipREADME.md");

		unzipFile(source, target);
	}

	public static void unzipFile(Path sourceZip, Path targetDir) {

		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceZip.toFile()))) {

			// list files in zip
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {

				boolean isDirectory = false;
				if (zipEntry.getName().endsWith(File.separator)) {
					isDirectory = true;
				}

				Path newPath = zipSlipProtect(zipEntry, targetDir);
				if (isDirectory) {
					Files.createDirectories(newPath);
				} else {
					if (newPath.getParent() != null) {
						if (Files.notExists(newPath.getParent())) {
							Files.createDirectories(newPath.getParent());
						}
					}
					// copy files
					Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
				}

				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir)
			throws IOException {

		// test zip slip vulnerability
		Path targetDirResolved = targetDir.resolve(zipEntry.getName());

		// make sure normalized file still has targetDir as its prefix
		// else throws exception
		Path normalizePath = targetDirResolved.normalize();
		if (!normalizePath.startsWith(targetDir)) {
			throw new IOException("Bad zip entry: " + zipEntry.getName());
		}
		return normalizePath;
	}






	public void makeFolderToZip(File dir, String zipDirName){
		try {
			populateFilesList(dir);
			//now zip files one by one
			//create ZipOutputStream to write to the zip file
			FileOutputStream fos = new FileOutputStream(zipDirName);
			ZipOutputStream zos = new ZipOutputStream(fos);
			for(String filePath : filesListInDir){
				System.out.println("Zipping "+filePath);
				//for ZipEntry we need to keep only relative file path, so we used substring on absolute path
				ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
				zos.putNextEntry(ze);
				//read the file and write to ZipOutputStream
				FileInputStream fis = new FileInputStream(filePath);
				byte[] buffer = new byte[1024];
				int len;
				while ((len = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}
				zos.closeEntry();
				fis.close();
			}
			zos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void populateFilesList(File dir) throws IOException {
		File[] files = dir.listFiles();
		for(File file : files){
			if(file.isFile()) filesListInDir.add(file.getAbsolutePath());
			else populateFilesList(file);
		}
	}

	private static void zipSingleFile(File file, String zipFileName) {
		try {
			//create ZipOutputStream to write to the zip file
			FileOutputStream fos = new FileOutputStream(zipFileName);
			ZipOutputStream zos = new ZipOutputStream(fos);
			//add a new Zip Entry to the ZipOutputStream
			ZipEntry ze = new ZipEntry(file.getName());
			zos.putNextEntry(ze);
			//read the file and write to ZipOutputStream
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}

			//Close the zip entry to write to zip file
			zos.closeEntry();
			//Close resources
			zos.close();
			fis.close();
			fos.close();
			System.out.println(file.getCanonicalPath()+" is zipped to "+zipFileName);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
