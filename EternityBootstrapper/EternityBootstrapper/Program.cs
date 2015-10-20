using System;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Linq;

namespace EternityBootstrapper {
	class Program {
		static void Main (string[] args) {
			var root = AppDomain.CurrentDomain.BaseDirectory;
			var java = Path.Combine(root, @"jre\bin\java.exe");
			var jar = Path.Combine(root, "jar");
			var src = Path.Combine(root, "src");
			Directory.SetCurrentDirectory(root);

			var zips = Directory.GetFiles(root, "*.zip");
			var max = zips.Select(zip => Convert.ToInt64(Path.GetFileNameWithoutExtension(zip)))
				.DefaultIfEmpty(-1)
				.Max();

			if (max > -1) {
				var mostRecentZipFilename = Convert.ToString(max) + ".zip";
				var mostRecentZip = Path.Combine(root, mostRecentZipFilename);
				var updateOutput = Path.Combine(root, "update");

				if (Directory.Exists(updateOutput)) {
					Directory.Delete(updateOutput, true);
				}

				Directory.CreateDirectory(updateOutput);
				ZipFile.ExtractToDirectory(mostRecentZip, updateOutput);

				var directories = Directory.GetDirectories(updateOutput);
				var update = directories[0];
				var jarBackup = Path.Combine(root, "jar.old");
				var srcBackup = Path.Combine(root, "src.old");

				if (Directory.Exists(jarBackup)) {
					Directory.Delete(jarBackup, true);
				}

				if (Directory.Exists(srcBackup)) {
					Directory.Delete(srcBackup, true);
				}

				Directory.Move(jar, jarBackup);
				Directory.Move(src, srcBackup);

				Directory.Delete(jar, true);
				Directory.Delete(src, true);

				Directory.CreateDirectory(jar);
				Directory.CreateDirectory(src);

				var updateJars = Directory.GetFiles(update, "*.jar");
				foreach (var newJar in updateJars) {
					File.Copy(newJar, Path.Combine(jar, Path.GetFileName(newJar)));
				}

				Directory.Move(Path.Combine(update, "ui"), Path.Combine(src, "ui"));

				foreach (var zip in zips) {
					File.Delete(zip);
				}
			}

			var jars = Directory.GetFiles(jar, "*.jar");
			max = jars.Select(f => Convert.ToInt64(Path.GetFileNameWithoutExtension(f)))
				.DefaultIfEmpty(-1)
				.Max();

			var mostRecentJarFilename = Convert.ToString(max) + ".jar";
			var mostRecentJar = Path.Combine(jar, mostRecentJarFilename);

			var process = new Process {
				StartInfo = {
					FileName = java,
					UseShellExecute = false,
					RedirectStandardOutput = false,
					WorkingDirectory = root,
					Arguments = "-jar -Djava.library.path=lib \"" + mostRecentJar + "\""
				}
			};
			process.Start();
		}
	}
}
