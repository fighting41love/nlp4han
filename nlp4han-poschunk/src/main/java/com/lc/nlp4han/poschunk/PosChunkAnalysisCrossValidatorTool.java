package com.lc.nlp4han.poschunk;

import java.io.File;
import java.io.IOException;

import com.lc.nlp4han.chunk.AbstractChunkAnalysisMeasure;
import com.lc.nlp4han.chunk.AbstractChunkSampleParser;
import com.lc.nlp4han.chunk.AbstractChunkAnalysisSample;
import com.lc.nlp4han.chunk.ChunkAnalysisContextGenerator;
import com.lc.nlp4han.chunk.ChunkAnalysisMeasureBIEO;
import com.lc.nlp4han.chunk.ChunkAnalysisMeasureBIEOS;
import com.lc.nlp4han.chunk.ChunkAnalysisMeasureBIO;
import com.lc.nlp4han.ml.util.MarkableFileInputStreamFactory;
import com.lc.nlp4han.ml.util.ObjectStream;
import com.lc.nlp4han.ml.util.PlainTextByLineStream;
import com.lc.nlp4han.ml.util.SequenceValidator;
import com.lc.nlp4han.ml.util.TrainingParameters;

/**
 * 交叉验证工具类
 */
public class PosChunkAnalysisCrossValidatorTool
{

	private static void usage()
	{
		System.out.println(PosChunkAnalysisCrossValidatorTool.class.getName()
				+ " -data <corpusFile> -type <type> -label <label> -encoding <encoding> [-folds <nFolds>] [-cutoff <num>] [-iters <num>]");
	}

	public static void main(String[] args)
			throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException
	{
		if (args.length < 1)
		{
			usage();
			return;
		}

		int cutoff = 3;
		int iters = 100;
		int folds = 10;
		// Maxent, Perceptron, MaxentQn, NaiveBayes
		String type = "Maxent";
		String scheme = "BIEO";
		File corpusFile = null;
		String encoding = "UTF-8";
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-data"))
			{
				corpusFile = new File(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-type"))
			{
				type = args[i + 1];
				i++;
			}
			else if (args[i].equals("-label"))
			{
				scheme = args[i + 1];
				i++;
			}
			else if (args[i].equals("-encoding"))
			{
				encoding = args[i + 1];
				i++;
			}
			else if (args[i].equals("-cutoff"))
			{
				cutoff = Integer.parseInt(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-iters"))
			{
				iters = Integer.parseInt(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-folds"))
			{
				folds = Integer.parseInt(args[i + 1]);
				i++;
			}
		}

		TrainingParameters params = TrainingParameters.defaultParams();
		params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(cutoff));
		params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(iters));
		params.put(TrainingParameters.ALGORITHM_PARAM, type.toUpperCase());

		ObjectStream<String> lineStream = new PlainTextByLineStream(new MarkableFileInputStreamFactory(corpusFile),
				encoding);
		AbstractChunkSampleParser parse = null;
		AbstractChunkAnalysisMeasure measure = null;
		SequenceValidator<String> sequenceValidator = null;

		if (scheme.equals("BIEOS"))
		{
			parse = new PosChunkSampleParserBIEOS();
			measure = new ChunkAnalysisMeasureBIEOS();
			sequenceValidator = new PosChunkAnalysisSequenceValidatorBIEOS();
		}
		else if (scheme.equals("BIEO"))
		{
			parse = new PosChunkSampleParserBIEO();
			measure = new ChunkAnalysisMeasureBIEO();
			sequenceValidator = new PosChunkAnalysisSequenceValidatorBIEO();
		}
		else
		{
			parse = new PosChunkSampleParserBIO();
			measure = new ChunkAnalysisMeasureBIO();
			sequenceValidator = new PosChunkAnalysisSequenceValidatorBIO();
		}

		ChunkAnalysisContextGenerator contextGen = new PosChunkAnalysisContextGeneratorConf();
		PosChunkAnalysisCrossValidation crossValidator = new PosChunkAnalysisCrossValidation(params);
		ObjectStream<AbstractChunkAnalysisSample> sampleStream = new PosChunkAnalysisSampleStream(lineStream, parse,
				scheme);

		crossValidator.evaluate(sampleStream, folds, contextGen, measure, sequenceValidator);
	}
}
