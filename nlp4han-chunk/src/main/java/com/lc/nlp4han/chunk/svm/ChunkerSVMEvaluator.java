package com.lc.nlp4han.chunk.svm;

import java.io.IOException;

import com.lc.nlp4han.chunk.AbstractChunkAnalysisMeasure;
import com.lc.nlp4han.chunk.AbstractChunkAnalysisSample;
import com.lc.nlp4han.chunk.ChunkAnalysisEvaluateMonitor;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosSample;
import com.lc.nlp4han.ml.util.Evaluator;

public class ChunkerSVMEvaluator extends Evaluator<AbstractChunkAnalysisSample>
{
	private ChunkerSVM chunkTagger;

	private AbstractChunkAnalysisMeasure measure;

	public ChunkerSVMEvaluator(ChunkerSVM chunkTagger, AbstractChunkAnalysisMeasure measure,
			ChunkAnalysisEvaluateMonitor... evaluateMonitors)
	{
		super(evaluateMonitors);
		this.chunkTagger = chunkTagger;
		this.measure = measure;
	}

	public ChunkerSVMEvaluator(ChunkerSVM chunkTagger)
	{
		this.chunkTagger = chunkTagger;
	}

	public void setChunkTagger(ChunkerSVM chunkTagger)
	{
		this.chunkTagger = chunkTagger;
	}

	public void setMeasure(AbstractChunkAnalysisMeasure measure)
	{
		this.measure = measure;
	}

	@Override
	protected AbstractChunkAnalysisSample processSample(AbstractChunkAnalysisSample sample)
	{

		ChunkerWordPosSample wordAndPOSSample = (ChunkerWordPosSample) sample;

		String[] wordsRef = wordAndPOSSample.getTokens();
		String[] chunkTagsRef = wordAndPOSSample.getTags();

		Object[] objectPosesRef = wordAndPOSSample.getAditionalContext();
		String[] posesRef = new String[objectPosesRef.length];
		for (int i = 0; i < posesRef.length; i++)
			posesRef[i] = (String) objectPosesRef[i];

		String[] chunkTagsPre = null;
		try
		{
			chunkTagsPre = chunkTagger.tag(wordsRef, posesRef);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// 将结果进行解析，用于评估
		ChunkerWordPosSample prediction = new ChunkerWordPosSample(wordsRef, posesRef, chunkTagsPre);
		prediction.setTagScheme(sample.getTagScheme());

		measure.update(wordsRef, chunkTagsRef, chunkTagsPre);
		return prediction;
	}

	public AbstractChunkAnalysisMeasure getMeasure()
	{
		return measure;
	}

}
