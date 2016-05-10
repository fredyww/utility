package org.panda.utility;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author Ozgun Babur
 */
public class FileUtil
{
	public static ZipEntry findEntryContainingNameInZIPFile(String zipFileName,
		String partOfEntryName)
	{
		return findEntryContainingNameInZIPFile(zipFileName, partOfEntryName, null);
	}

	public static ZipEntry findEntryContainingNameInZIPFile(String zipFileName,
		String partOfEntryName, String tabooPartOfEntryName)
	{
		try
		{
			ZipFile zipFile = new ZipFile(zipFileName);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements())
			{
				ZipEntry zipEntry = entries.nextElement();
				if (!zipEntry.isDirectory())
				{
					String fileName = zipEntry.getName();
					if (fileName.contains(partOfEntryName) &&
						(tabooPartOfEntryName == null || !fileName.contains(tabooPartOfEntryName)))
					{
						return zipEntry;
					}
				}
			}
			zipFile.close();
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
			return null;
		}
		return null;
	}

	public static boolean extractEntryContainingNameInTARGZFile(String targzFileName,
		String partOfEntryName, String extractedName)
	{
		try
		{
			TarArchiveEntry entry;

			TarArchiveInputStream is = new TarArchiveInputStream(new GZIPInputStream(
				new FileInputStream(targzFileName)));

			while ((entry = is.getNextTarEntry()) != null)
			{
				if (!entry.isDirectory())
				{
					if (entry.getName().contains(partOfEntryName))
					{
						byte [] btoRead = new byte[1024];

						BufferedOutputStream bout =new BufferedOutputStream(
							new FileOutputStream(extractedName));

						int len;
						while((len = is.read(btoRead)) != -1)
						{
							bout.write(btoRead,0,len);
						}

						bout.close();
						is.close();

						return true;
					}
				}
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		return false;
	}

	public static boolean extractAllEntriesContainingNameInTARGZFile(String targzFileName,
		String partOfEntryName, String dir)
	{
		try
		{
			TarArchiveEntry entry;

			TarArchiveInputStream is = new TarArchiveInputStream(new GZIPInputStream(
				new FileInputStream(targzFileName)));

			boolean success = false;

			while ((entry = is.getNextTarEntry()) != null)
			{
				if (entry.isDirectory()) continue;
				else
				{
					String name = entry.getName();
					if (name.contains("/")) name = name.substring(name.lastIndexOf("/") + 1);
					if (name.contains(partOfEntryName))
					{
						byte [] btoRead = new byte[1024];

						BufferedOutputStream bout =new BufferedOutputStream(
							new FileOutputStream(dir + File.separator + name));

						int len;
						while((len = is.read(btoRead)) != -1)
						{
							bout.write(btoRead,0,len);
						}

						bout.close();
						success = true;
					}
				}
			}
			is.close();
			return success;
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		return false;
	}

	public static boolean extractEntryContainingNameInZipFile(String zipFileName,
		String partOfEntryName, String tabooPartOfEntryName, String extractedName)
	{
		try
		{
			ZipEntry entry = findEntryContainingNameInZIPFile(zipFileName, partOfEntryName,
				tabooPartOfEntryName);

			if (entry == null) return false;

			OutputStream out = new FileOutputStream(extractedName);
			FileInputStream fin = new FileInputStream(zipFileName);
			BufferedInputStream bin = new BufferedInputStream(fin);
			ZipInputStream zin = new ZipInputStream(bin);
			ZipEntry ze;
			while ((ze = zin.getNextEntry()) != null) {
				if (ze.getName().equals(entry.getName())) {
					byte[] buffer = new byte[8192];
					int len;
					while ((len = zin.read(buffer)) != -1) {
						out.write(buffer, 0, len);
					}
					out.close();
					break;
				}
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			return false;
		}

		return true;
	}

	public static String getFileContent(String filename)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				sb.append(line).append("\n");
			}

			reader.close();
			return sb.toString();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static void printLines(String filename, String partialContent) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));

		int i = 0;
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			i++;
			if (line.contains(partialContent))
			{
				System.out.println("Line " + i + ": " + line);
			}
		}

		reader.close();
	}

	public static void printLines(String filename, int fromLine, int toLine) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));

		int i = 0;
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			i++;
			if (i >= fromLine && i <= toLine)
			{
				System.out.println(line);
			}
			if (i > toLine) break;
		}

		reader.close();
	}

	public static long countLines(String filename) { try
	{
		return Files.lines(Paths.get(filename)).count();
	}
	catch (IOException e){throw new RuntimeException(e);}}

	public static void copyFile(String src, String dest) throws IOException
	{
		File sourceFile = new File(src);
		File destFile = new File(dest);

		if(!destFile.exists())
		{
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		catch (IOException e){e.printStackTrace();}
		finally {
			if(source != null) {
				source.close();
			}
			if(destination != null) {
				destination.close();
			}
		}
	}

	public static void delete(File dir)
	{
		if (dir.isDirectory())
		{
			for (File file : dir.listFiles())
			{
				delete(file);
			}
		}
		dir.delete();
	}

	public static void write(String line, Writer writer)
	{
		try
		{
			writer.write(line);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void writeln(String line, Writer writer)
	{
		write(line + "\n", writer);
	}

	//----- Section: XLSX related --------------------------------------------------------------------------------------

	/**
	 * Reads a worksheet from a .xlsx formatted file and returns it as a stream of String[].
	 *
	 * Code lifted from:
	 * http://java67.blogspot.com/2014/09/how-to-read-write-xlsx-file-in-java-apache-poi-example.html
	 * http://stackoverflow.com/questions/24511052/how-to-convert-an-iterator-to-a-stream
	 */
	public static Stream<String[]> getXLSXAsStream(String filename, String sheetName)
	{
		try
		{
			XSSFWorkbook myWorkBook = new XSSFWorkbook(new FileInputStream(new File(filename)));
			// Grab desired sheet from the XLSX workbook
			XSSFSheet mySheet = myWorkBook.getSheet(sheetName);
			// Get iterator to all the rows in the sheet
			Iterator<Row> rowIterator = mySheet.iterator();
			// Wrap that iterator with another that converts row to String[]
			XLSXRowIterator iter = new XLSXRowIterator(rowIterator);
			// Convert to stream
			Iterable<String[]> iterable = () -> iter;
			return StreamSupport.stream(iterable.spliterator(), false);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	static class XLSXRowIterator implements Iterator<String[]>
	{
		Iterator<Row> innerIter;

		public XLSXRowIterator(Iterator<Row> innerIter)
		{
			this.innerIter = innerIter;
		}

		@Override
		public boolean hasNext()
		{
			return innerIter.hasNext();
		}

		@Override
		public String[] next()
		{
			Row row = innerIter.next();
			List<String> list = new ArrayList<>();
			row.forEach(cell -> list.add(
				cell.getCellType() == Cell.CELL_TYPE_STRING ? cell.getStringCellValue() :
				cell.getCellType() == Cell.CELL_TYPE_NUMERIC ? Double.toString(cell.getNumericCellValue()) : ""));
			return list.toArray(new String[list.size()]);
		}
	}

	public static List<String> getXLSXSheetNames(String filename) { try
	{
		// Generates a workbook instance for XLSX file
		XSSFWorkbook myWorkBook = new XSSFWorkbook(new FileInputStream(new File(filename)));

		return IntStream.range(0, myWorkBook.getNumberOfSheets()).boxed().map(myWorkBook::getSheetName)
			.collect(Collectors.toList());
	}
	catch (IOException e){throw new RuntimeException(e);}}


	//--- Section: SIF files ------------------------------------------------------------------------------------------|

	public static void replaceNodeNamesInSIFFile(String filename, Map<String, String> substutitionMap) { try
	{
		String tmpFile = filename + ".tmp";
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(tmpFile));
		File file = new File(filename);
		Scanner sc = new Scanner(file);
		while (sc.hasNextLine())
		{
			String[] token = sc.nextLine().split("\t");
			if (token.length > 2)
			{
				if (substutitionMap.containsKey(token[0])) token[0] = substutitionMap.get(token[0]);
				if (substutitionMap.containsKey(token[2])) token[2] = substutitionMap.get(token[2]);
			}
			writer.write(token[0]);
			for (int i = 1; i < token.length; i++)
			{
				writer.write("\t" + token[i]);
			}
			writer.write("\n");
		}
		writer.close();

		file.delete();
		new File(tmpFile).renameTo(file);
	}
	catch (IOException e){throw new RuntimeException(e);}}

	public static Set<String> getNodeNamesInSIFFile(String filename) { try
	{
		return Files.lines(Paths.get(filename)).map(line -> line.split("\t")).filter(token -> token.length >= 3)
			.map(token -> new String[]{token[0], token[2]}).flatMap(Arrays::stream).collect(Collectors.toSet());
	}
	catch (IOException e){throw new RuntimeException(e);}}

	public static void main(String[] args) throws IOException
	{
//		printLines("SIFWithLoc.sif", "controls-transport-of");
		System.out.println(Math.sqrt(countLines("/home/babur/Documents/Temp/distances.txt")));
	}
}
