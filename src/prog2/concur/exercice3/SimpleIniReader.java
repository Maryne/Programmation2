package prog2.concur.exercice3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Cette classe permet de lire un fichier de configuration au format *.ini. Elle
 * ne prend pas en compte toutes les spécificités d'un fichier ini, tels que les
 * sections ou les paramètres en quotes !
 * 
 * @author Papillon Maxence & Maryne Teissier
 */
public class SimpleIniReader {
	private File _iniFile;

	/**
	 * Instancie la classe à partir du chemin du fichier de configuration.
	 * 
	 * @param path
	 *            Chemin vers le fichier de configuration
	 * @throws IOException
	 */
	public SimpleIniReader(String path) throws IOException {
		_iniFile = new File(path);

		if (!_iniFile.exists()) {
			throw new FileNotFoundException();
		}
	}

	/**
	 * Permet de récupèrer la valeur d'une clef.
	 * 
	 * @param key
	 *            Clef.
	 * @return Valeur du la clef.
	 * @throws IOException
	 */
	public String get(String key) throws IOException {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(_iniFile));
			String line;

			while ((line = reader.readLine()) != null) {
				// Si la ligne est un commentaire ou est vide, on ne fait rien.
				if((line.length() != 0) && (line.charAt(0) != ';')) {
					String[] cfgDta = line.split("=");					
					if(cfgDta[0].equals(key)) {
						return cfgDta[1];
					}
				}
			}
		} finally {
			reader.close();
		}
		return null;
	}
}
