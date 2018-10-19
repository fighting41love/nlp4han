package com.lc.nlp4han.chunk.svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.lc.nlp4han.chunk.AbstractChunkAnalysisSample;
import com.lc.nlp4han.chunk.Chunk;
import com.lc.nlp4han.chunk.ChunkAnalysisContextGenerator;
import com.lc.nlp4han.chunk.Chunker;
import com.lc.nlp4han.chunk.svm.libsvm.svm_model;
import com.lc.nlp4han.chunk.wordpos.ChunkAnalysisWordPosSample;
import com.lc.nlp4han.chunk.wordpos.ChunkAnalysisWordPosSampleEvent;
import com.lc.nlp4han.ml.model.Event;
import com.lc.nlp4han.ml.util.ObjectStream;

public class ChunkAnalysisSVMME implements Chunker
{
	private List<String> FeatureStructure = new ArrayList<String>();		//存储用到的特征，如w_2, p_2，用以记录各特征对应的序号，特征的（index+1）为SVM标准输入格式中该特征的序号
	private List<String> ClassificationResults = new ArrayList<String>();	//存储分类结果，如BNP_B, BNP_I, BNP_E, O，（index+1）为SVM标准分类结果
	private Map<String, Map<String, Integer>> Features = new HashMap<String, Map<String, Integer>>();	//记录所有具体的特征，为每一特征赋值
	private ChunkAnalysisContextGenerator contextgenerator;
	private List<Integer> NumberOfClassification = new ArrayList<Integer>();
	private svm_model model;
	private String label;
	private ScaleInfo scaleInfo= null;
	
	public ChunkAnalysisSVMME()
	{
		
	}
	public ChunkAnalysisSVMME(ChunkAnalysisContextGenerator contextgenerator, svm_model model, String filePath, String label)
	{
		this(contextgenerator, model, label);
		init(filePath);
	}
	
	public ChunkAnalysisSVMME(ChunkAnalysisContextGenerator contextgenerator, svm_model model, String label)
	{
		super();
		this.contextgenerator = contextgenerator;
		this.model = model;
		this.label = label;
	}
	
	public ScaleInfo getScaleInfo()
	{
		return scaleInfo;
	}
	public List<String> getFeatureStructure()
	{
		return FeatureStructure;
	}
	
	public List<String> getClassificationResults()
	{
		return ClassificationResults;
	}
	
	public Map<String, Map<String, Integer>> getFeatures()
	{
		return Features;
	}
	
	public List<Integer> getNumberOfClassification()
	{
		return this.NumberOfClassification;
	}
	
	/**
	 * 直接初始化用于将文本转换成svm输入格式的三个参数
	 * @param FeatureStructure
	 * @param ClassificationResults
	 * @param Features
	 */
	public void init(List<String> FeatureStructure, List<String> ClassificationResults, Map<String, Map<String, Integer>> Features, List<Integer> NumberOfClassification)
	{
		this.Features = Features;
		this.FeatureStructure = FeatureStructure;
		this.ClassificationResults = ClassificationResults;
		this.NumberOfClassification = NumberOfClassification;
	}

	/**
	 * 从文件中初始化用于将文本转换成svm输入格式的参数
	 * @param filePath
	 */
	public void init(String filePath)
	{
		FileInputStream fis = null;
		BufferedReader reader = null;

		try
		{
			fis = new FileInputStream(new File(filePath));

			reader = new BufferedReader(new InputStreamReader(fis, "utf-8"));

			String tempString = null;
			
			int currentLine = 1;
			int num1 = 0;
			int num2 = 0;
			String key = null;
			int num3 = 0;
			Map<String, Integer> value = null;
			
			int lower = 1;
			int upper = 1;
			boolean scaleFlag = false;

			while ((tempString = reader.readLine()) != null)
			{
				String[] strs = null;
				if (currentLine == 1)
				{
					if (tempString.split("=")[1].equals("true"))
					{
						scaleFlag = true;
					}
					currentLine++;
				}
				else if (currentLine == 2)
				{
					if (scaleFlag)
					{
						lower = Integer.parseInt(tempString.split("=")[1]);
					}
					currentLine++;
				}
				else if (currentLine == 3)
				{
					if (scaleFlag)
					{
						upper = Integer.parseInt(tempString.split("=")[1]);
					}
					currentLine++;
				}
				else if (currentLine == 4)
				{
					if (scaleFlag)
					{
						String[] str = tempString.split(" ");
						int[] ranges = new int[str.length];
						for (int i=0 ; i<str.length ; i++)
						{
							ranges[i] = Integer.parseInt(str[i]);
						}
						scaleInfo = new ScaleInfo(lower, upper, ranges);
					}
					currentLine++;
				}
				else if (currentLine == 5)
				{
					 strs = tempString.split("=");
					 num1 = str2int(strs[1]) + currentLine;
					 num2 = num1 +1;
					 currentLine++;
				}
				else if (currentLine <= num1)
				{
					FeatureStructure.add(tempString);
					currentLine++;
				}
				else if (currentLine == num1 + 1)
				{
					strs = tempString.split("=");
					num2 = str2int(strs[1]) + currentLine;
					num3 = num2;
					currentLine++;
				}
				else if (currentLine <= num2)
				{
					ClassificationResults.add(tempString);
					currentLine++;
				}
				else
				{
					if (currentLine == num3 + 1)
					{
						strs = tempString.split(" ");
						if (strs.length == 2 && strs[0].matches("[a-z]+") && strs[1].matches("[0-9]+"))
						{
							value = new HashMap<String, Integer>();
							key = strs[0];
							int tmp = str2int(strs[1]);
							if (tmp % 100 == 0)
							{
								num3 += tmp/100 + 1;
							}
							else
							{
								num3 += tmp/100 + 2;
							}
						}
						currentLine++;
					}
					else
					{
						strs = tempString.split(" ");
						for (int j=0 ; j<strs.length ; j++)
						{
							//TODO: 注意，此处的值为int型
							value.put(strs[j].split("=")[0], str2int(strs[j].split("=")[1]));
						}
						if (currentLine == num3)
						{
							Features.put(key, value);
						}
						currentLine++;
					}
				}
			}

			reader.close();

		}
		catch (IOException e)
		{

			e.printStackTrace();

		}
		finally
		{

			if (reader != null)
			{

				try
				{
					reader.close();
				}
				catch (IOException e1)
				{

				}

			}

		}
		
	}

	@Override
	public Chunk[] parse(String sentence)
	{
		String[] wordTags = sentence.split(" +");
		List<String> words = new ArrayList<>();
		List<String> poses = new ArrayList<>();

		
		for (String wordTag : wordTags)
		{
			words.add(wordTag.split("/")[0]);
			poses.add(wordTag.split("/")[1]);
		}

		String[] chunkTypes = null;
		try
		{
			chunkTypes = tag(words.toArray(new String[words.size()]), poses.toArray(new String[poses.size()]));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		AbstractChunkAnalysisSample sample = new ChunkAnalysisWordPosSample(words.toArray(new String[words.size()]),
				poses.toArray(new String[poses.size()]), chunkTypes);
		sample.setTagScheme(label);

		return sample.toChunk();
	}

	@Override
	public Chunk[][] parse(String sentence, int k)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public String[] tag(String[] words, String[] poses) throws IOException
	{
		String[] chunkTags = new String[words.length];
		String line = null;
		
		for (int i=0 ; i<words.length ; i++)
		{
			String[] context = contextgenerator.getContext(i, words, chunkTags, poses);
			line = "1 " + SVMStandardInput.getSVMStandardFeaturesInput(context, FeatureStructure, Features, scaleInfo);	//<label> <index1>:<value1> <index2>:<value2> ... 预测时，label可以为任意值
			String tag = predict(line, model);
			chunkTags[i] = tag;
		}
		return chunkTags;
	}
	
	
	/**
	 * 根据模型model，调用libsvm预测line的结果，line为libsvm的标准输入格式，形如 2 4:5 7:3 8:2....
	 */
	public String predict(String line, svm_model model) throws IOException
	{
		//TODO
		String v = SVMPredict.predict(line, model, 0);
		String result = transform(v);
		return result;
	}

	/**
	 * 将libsvm预测的结果（数字）转换成组块标注
	 */
	private String transform(String v)
	{
		int t = str2int(v);
		String result = ClassificationResults.get(t-1);
		return result;
	}
	
	private int str2int(String str)
	{
		return Integer.valueOf(str.trim().split("\\.")[0]);
	}
	
	private static double atof(String s)
	{
		double d = Double.valueOf(s).doubleValue();
		if (Double.isNaN(d) || Double.isInfinite(d))
		{
			System.err.print("NaN or Infinity in input\n");
			System.exit(1);
		}
		return(d);
	}
	
	public svm_model train(ObjectStream<AbstractChunkAnalysisSample> sampleStream, String[] arg,
			ChunkAnalysisContextGenerator contextGen) throws IOException
	{
		ObjectStream<Event> es = new ChunkAnalysisWordPosSampleEvent(sampleStream, contextGen);
		String[] input = SVMStandardInput.standardInput(es);
		init(SVMStandardInput.getFeatureStructure(), SVMStandardInput.getClassificationResults(), SVMStandardInput.getFeatures(), SVMStandardInput.getNumberOfClassification());
		if (scale(arg))
		{
			
			scaleInfo.ranges = SVMStandardInput.getScaleRanges(FeatureStructure, Features);
			SVMStandardInput.scale(input, scaleInfo, FeatureStructure);
		}
		SVMTrain t = new SVMTrain();
		svm_model m = t.run(arg, input, false, null);
		return m;
		
	}
	
	private boolean scale(String[] args)
	{
		boolean result = false;
		int lower = 1;
		int upper = 1;
		for (int i=0 ; i<args.length ; i++)
		{
			if (args[i].equals("-l"))
			{
				result = true;
				lower = Integer.parseInt(args[i+1]);
				i++;
			}
			else if (args[i].equals("-u"))
			{
				result = true;
				upper = Integer.parseInt(args[i+1]);
				i++;
			}
		}
		
		if (result)
		{
			scaleInfo = new ScaleInfo(lower, upper);
		}
		return result;
	}
}
