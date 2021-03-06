package com.lc.nlp4han.constituent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 和括号表达式表示相关的工具方法
 * 
 * @author 刘小峰
 * @author 王馨苇
 *
 */
public class BracketExpUtil
{
	/**
	 * 从字符串中读取一个或多个括号表达式
	 * 
	 * 一行最多只能有一个完整的括号表达式
	 * 
	 * @param str
	 * @return 括号表达式列表
	 * @throws IOException
	 */
	public static ArrayList<String> readBrackets(String str) throws IOException
	{
		BufferedReader in = new BufferedReader(new StringReader(str));
		ArrayList<String> brackets = new ArrayList<String>();
		String line = "";
		String bracketStr = "";
		int left = 0;
		int right = 0;
		while ((line = in.readLine()) != null)
		{
			if (line != "" && !line.equals(""))
			{
				line = line.replaceAll("\n", "");
				char[] chars = line.trim().toCharArray();
				
				bracketStr += line.trim();
				
				for (int i = 0; i < chars.length; i++)
				{
					if (chars[i] == '(')
					{
						left++;
					}
					else if (chars[i] == ')')
					{
						right++;
					}
				}

				if (left == right && left>0)
				{
					brackets.add(bracketStr);
					
					bracketStr = "";
					left = right = 0;
				}
			}
		}

		return brackets;
	}

	// 去掉最外围一层的括号和文法符号
	public static TreeNode generateTreeNoTopBracket(String bracketStr)
	{
		bracketStr = formatNoTopBracket(bracketStr);
		
		return generateProcess(bracketStr);
	}

	/**
	 * 生成树，但是不去掉最外层括号表达式
	 */
	public static TreeNode generateTree(String bracketStr)
	{
		
		bracketStr = format(bracketStr);
		return generateProcess(bracketStr);
	}

	/**
	 * 遍历树，将树中的-RRB-和-LRB-转换为左右括号
	 * 
	 * @param 根节点
	 * @return
	 */
	public static void escapeBracketTree(TreeNode node)
	{
		if (node.getChildrenNum() == 0)
		{
			if (node.getNodeName().equals("-LRB-"))
			{
				node.setNewName("(");
			}
			else if (node.getNodeName().equals("-RRB-"))
			{
				node.setNewName(")");
			}
			return;
		}
		
		for (TreeNode childNode : node.getChildren())
		{
			escapeBracketTree(childNode);
		}
	}

	/**
	 * 将括号表达式去掉空格转成列表的形式
	 * 
	 * 列表中含: 括号、空格和终结符、非终结符
	 * 
	 * @param bracketStr
	 *            括号表达式
	 * @return
	 */
	public static List<String> stringToList(String bracketStr)
	{
		List<String> parts = new ArrayList<String>();
		for (int index = 0; index < bracketStr.length(); ++index)
		{
			char c = bracketStr.charAt(index);
			if (c == '(' || c == ')' || c == ' ')
			{
				parts.add(Character.toString(c));
			}
			else
			{
				for (int i = index + 1; i < bracketStr.length(); ++i)
				{
					char c2 = bracketStr.charAt(i);
					if (c2 == '(' || c2 == ')' || c2 == ' ')
					{
						parts.add(bracketStr.substring(index, i));
						index = i - 1;
						break;
					}
				}
			}
		}
		return parts;
	}

	/**
	 * 格式化为形如：A(B1(C1 d1)(C2 d2))(B2 d3) 的括号表达式。叶子及其父节点用一个空格分割，其他字符紧密相连。
	 * 
	 * 去掉最外围的一对括号。
	 * 
	 * @param bracketStr
	 *            括号表达式
	 */
	public static String formatNoTopBracket(String bracketStr)
	{
		bracketStr = bracketStr.trim();
		
		// 去除最外围的括号
		bracketStr = bracketStr.substring(1, bracketStr.length() - 1).trim();

		return formatProcess(bracketStr);
	}

	/**
	 * 格式化为形如：(A(B1(C1 d1)(C2 d2))(B2 d3)) 的括号表达式。叶子及其父节点用一个空格分割，其他字符紧密相连.
	 * 
	 * 不去掉最外层括号表达式
	 * 
	 * @param bracketStr
	 *            括号表达式
	 * @return
	 */
	public static String format(String bracketStr)
	{
		bracketStr = bracketStr.trim();
		return formatProcess(bracketStr);
	}

	private static String formatProcess(String bracketStr)
	{
		// 所有空白符替换成一位空格
		bracketStr = bracketStr.replaceAll("\\s+", " ");

		// 去掉 ( 和 ) 前的空格
		String newTree = "";
		for (int c = 0; c < bracketStr.length(); ++c)
		{
			if (bracketStr.charAt(c) == ' ' && (bracketStr.charAt(c + 1) == '(' || bracketStr.charAt(c + 1) == ')'))
			{
				// 跳过空格
				continue;
			}
			else
			{
				newTree = newTree + (bracketStr.charAt(c));
			}
		}

		return newTree;
	}

	private static TreeNode generateProcess(String bracketStr)
	{
		List<String> parts = stringToList(bracketStr);

		Stack<TreeNode> tree = new Stack<TreeNode>();
		int wordindex = 0;
		for (int i = 0; i < parts.size(); i++)
		{
			String str = parts.get(i);
			if (!str.equals(")") && !str.equals(" ")) // 左括号或文法符号
			{
				TreeNode tn = new TreeNode(str);
				tn.setFlag(true);
				tree.push(tn);
			}
			else if (str.equals(" "))
			{

			}
			else if (str.equals(")"))
			{
				Stack<TreeNode> temp = new Stack<TreeNode>();
				while (!tree.peek().getNodeName().equals("("))
				{
					if (!tree.peek().getNodeName().equals(" "))
					{
						temp.push(tree.pop());
					}
				}
				
				tree.pop();
				TreeNode node = temp.pop();
				while (!temp.isEmpty())
				{
					temp.peek().setParent(node);
					if (temp.peek().getChildren().size() == 0)
					{
						TreeNode wordindexnode = temp.pop();
						wordindexnode.setWordIndex(wordindex++);
						node.addChild(wordindexnode);
					}
					else
					{
						node.addChild(temp.pop());
					}
				}
				
				tree.push(node);
			}
		}
		
		TreeNode treeStruct = tree.pop();
		escapeBracketTree(treeStruct);
		
		return treeStruct;
	}

	/**
	 * 从括号表达式提取词性标注串
	 * 
	 * 括号表达式中括号单词会反转义
	 * 
	 * @param bracketStr
	 * @param sep
	 * @return
	 */
	public static String extractWordAndPos(String bracketStr, String sep)
	{
		bracketStr = format(bracketStr);
		
		List<String> parts = stringToList(bracketStr);
		
		String result = "";
		Stack<String> stack = new Stack<String>();
		for (int i = 0; i < parts.size(); i++)
		{
			if (!parts.get(i).equals(")") && !parts.get(i).equals(" "))
			{
				stack.push(parts.get(i));
			}
			else if (parts.get(i).equals(" "))
			{
	
			}
			else if (parts.get(i).equals(")"))
			{
				if (!stack.isEmpty())
				{
					String word = stack.pop();
					String pos= stack.pop();
					if(word.equals("-RRB-")) {
						word=")";
					}else if(word.equals("-LRB-")) {
						word="(";
					}
					result += pos + sep + word + " ";
				}
				stack.clear();
				;
			}
		}
	
		return result;
	}
}
