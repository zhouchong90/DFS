import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import evaluation.TopicCoherence;
import extractDFS.JSMergeLoss;
import extractDFS.KLMean;
import extractDFS.TFIDF;
import global.CmdOption;
import task.AMCModelRunningTask;

public class Runner {
	// The input file should contain a data file and a dictionary
	private String inputFileFolder = "./input/";

	private String outputFileFolder = "./output/";

	// The input is a one doc per line format for the raw forum data.
	// Doc file is one doc per line, word_id:occurrance
	// dictionary file starts numbering at 0
	public static void main(String[] args) throws IOException {

		BufferedWriter bw = new BufferedWriter(new FileWriter("logger.txt", true));

		int[] topicList = new int[] { 15, 20, 25, 30, 35, 40, 45, 50};
		String[] metricList = new String[] { "LMI", "KLD" };
		String[] domain = new String[]{"Mooc", "Breast", "Machine"};

		boolean runDFS1 = true, runDFS2 = true, runDFS3 = true;
		boolean runAMC = true;
		boolean runEval = true;

		boolean runAll = true;
		boolean reRunAMC = true;

		for (int topics : topicList) {
			for (String metric : metricList) {
				System.out.println("Experiment, Syllabus, " + metric + " , topic = " + topics);
				bw.write("Experiment, Syllabus, " + metric + " , topic = " + topics);
				bw.newLine();

				Runner p = new Runner();

				if (runDFS1 || runAll) {
					runDFS(p.inputFileFolder, "Mooc", metric, 20);
				}
				if (runDFS2 || runAll) {
					runDFS(p.inputFileFolder, "Breast", metric, 20);
				}
				if (runDFS3 || runAll) {
					runDFS(p.inputFileFolder, "Machine", metric, 20);
				}

				if (runAMC || runAll) {

					CmdOption cmdOption = new CmdOption();
					cmdOption.nTopics = topics;
					cmdOption.input100ReviewCorporeaDirectory = p.inputFileFolder;
					cmdOption.outputRootDirectory = p.outputFileFolder;
					cmdOption.nthreads = 3;
					cmdOption.randomSeed = 87005;
					cmdOption.alpha = 1.0;
					cmdOption.beta = 0.1;
					cmdOption.nBurnin = 100; // 200
					cmdOption.sampleLag = 10;// 10
					cmdOption.nIterations = 500;// 1000
					cmdOption.twords = 30;// top topic words
					cmdOption.reRunAMC = reRunAMC;

					try {
						long startTime = System.currentTimeMillis();
						System.out.println("Program Starts.");

						// Check if the input directory is valid.
						if (new File(cmdOption.input100ReviewCorporeaDirectory).listFiles() == null)
						// || new
						// File(cmdOption.input1000ReviewCorporeaDirectory).listFiles()
						// == null)
						{
							System.err.println("Input directory is not correct, program exits!");
							return;
						}

						// Run the proposed method on the multiple domains.
						AMCModelRunningTask task = new AMCModelRunningTask(cmdOption);
						task.run();

						System.out.println("Program Ends.");
						long endTime = System.currentTimeMillis();
						showRunningTime(endTime - startTime);
					} catch (Exception e) {
						System.out.println("Error in program: " + e.getMessage());
						e.printStackTrace();
						return;
					}
				}

				if (runEval || runAll) {
					TopicCoherence.runEval(bw, "AMC", domain);
					bw.newLine();
					bw.newLine();
					bw.flush();
				}
			}
		}

		bw.close();
	}

	private static void showRunningTime(long time) {
		System.out.println("Elapsed time: " + String.format("%.3f", (time) / 1000.0) + " seconds");
		System.out.println("Elapsed time: " + String.format("%.3f", (time) / 1000.0 / 60.0) + " minutes");
		System.out.println("Elapsed time: " + String.format("%.3f", (time) / 1000.0 / 3600.0) + " hours");
	}

	private static void runDFS(String input, String dataSource, String metric, int topk) {
		String inFolder = input + File.separator + dataSource + File.separator;

		if (metric.equals("KLD")) {
			KLMean dfs = new KLMean(inFolder + "domaindoc", inFolder + "dfs.csv", topk);
			dfs.run();
		} else if (metric.equals("LMI")) {
			JSMergeLoss dfs = new JSMergeLoss(inFolder + "domaindoc", inFolder + "dfs.csv", topk);
			dfs.run();
		} else if (metric.equals("None")){
			FileWriter fw;
			try {
				fw = new FileWriter(inFolder + "dfs.csv");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (metric.equals("ANT"))
		{
			//pass
		}
		else {
			System.err.println("Unrecognized");
		}
	}

}
