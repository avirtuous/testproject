package com.example.testproject;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SpringBootTest
class TestprojectApplicationTests {

	@Test
	void compressor() {
		makeFolderToZip("C:\\Users\\white\\Desktop\\ziptest\\example.zip","C:\\Users\\white\\Desktop\\ziptest");
		}


	@Test
	void unZipTest(){
		Path source = Paths.get("C:\\Users\\white\\Desktop\\manifest_dev.zip");
		Path target = Paths.get("C:\\tmp");

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

	public void makeFolderToZip(String zipFileName, String folderLocation){
		ZipFile zipFile = new ZipFile(zipFileName);
		try {
			zipFile.addFolder(new File(folderLocation));
		} catch (ZipException e) {
			e.printStackTrace();
		}
	}



}
