package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) throws Exception{
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		// 全てのファイルを取得
		File[] files = new File("C:\\Users\\takai.ryuta\\Desktop\\売り上げ集計課題").listFiles();


		// ファイル情報を格納するList
		List<File> rcdFiles = new ArrayList<>();

		// 売上ファイルであればListに追加
		for(int i = 0; i < files.length; i++) {
			if(files[i].getName().matches("^[0-9]{8}.+rcd$")) {
				rcdFiles.add(files[i]);
			}
		}

		// 売上ファイルの数だけ繰り返す
		for(int i = 0; i < rcdFiles.size(); i++) {
			// ファイルを取り出す
			File file = rcdFiles.get(i);

			// ファイルの中身を読む
			BufferedReader br = new BufferedReader(new FileReader(file));
				String code = br.readLine();
				String sales = br.readLine();

				// 売上の型変換
				long fileSale = Long.parseLong(sales);

				// 売上の加算
				Long saleAmount = branchSales.get(code) + fileSale;

				// 加算した売上を追加
				branchSales.put(code, saleAmount);
				br.close();
		}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}
	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");

				//Mapに追加する2つの情報をputの引数として指定
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);

				System.out.println(line);

			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) throws IOException {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		// 書き出す準備
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path, fileName)));

		// 支店コードを取り出してその分繰り返す
		for (String key : branchNames.keySet()) {

			// 支店名と売上金額を取得
			String name = branchNames.get(key);
			Long amount = branchSales.get(key);

			// 支店コード、支店名、売上金額をファイルに書き込む
			bw.write(key + "," + name + "," + amount);

			// 改行
			bw.newLine();
		}

		// 閉じる
		bw.close();
		return true;
	}

}
