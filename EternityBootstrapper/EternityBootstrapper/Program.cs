using System;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;

namespace EternityBootstrapper {
	class Program {
		static void Main (string[] args) {
			string root = AppDomain.CurrentDomain.BaseDirectory;
			Console.WriteLine(root);
			Directory.SetCurrentDirectory(root);

			string java = Path.Combine(root, @"jre\bin\java.exe");
			string jar = Path.Combine(root, "jar");
			string ui = Path.Combine(root, "src");

			string[] jars = Directory.GetFiles(jar, "*.jar");
			long max = -1;
			foreach (string candidate in jars) {
				long timestamp = Convert.ToInt64(Path.GetFileNameWithoutExtension(candidate));
				if (timestamp > max) {
					max = timestamp;
				}
			}

			string mostRecentJarFilename = Convert.ToString(max) + ".jar";
			string mostRecentJar = Path.Combine(root, jar, mostRecentJarFilename);

			string[] zips = Directory.GetFiles(ui, "*.zip");
			max = -1;
			foreach (string candidate in zips) {
				long timestamp = Convert.ToInt64(Path.GetFileNameWithoutExtension(candidate));
				if (timestamp > max) {
					max = timestamp;
				}
			}

			if (max > -1) {
				string mostRecentZipFilename = Convert.ToString(max) + ".zip";
				string mostRecentZip = Path.Combine(root, ui, mostRecentZipFilename);
				string oldUI = Path.Combine(root, ui, "ui");
				string backupUI = Path.Combine(root, ui, "ui.bak");
				Directory.Move(oldUI, backupUI);
				ZipFile.ExtractToDirectory(mostRecentZip, ui);
				Directory.Delete(backupUI, true);

				foreach (string candidate in zips) {
					File.Delete(candidate);
				}
			}

			Process process = new Process();
			process.StartInfo.FileName = java;
			process.StartInfo.UseShellExecute = false;
			process.StartInfo.RedirectStandardOutput = false;
			process.StartInfo.WorkingDirectory = root;
			process.StartInfo.Arguments = "-jar -Djava.library.path=lib \"" + mostRecentJar + "\"";
			process.Start();
		}
	}
}
