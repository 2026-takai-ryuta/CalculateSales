package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// 商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LST = "commodity.lst";

	// 商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String BRANCH_FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String COMMODITY_FILE_NOT_EXIST = "商品定義ファイルが存在しません";
	private static final String BRANCH_FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String COMMODITY_FILE_INVALID_FORMAT = "商品定義ファイルのフォーマットが不正です";
	private static final String FILE_NAME_ORDER_ERROR = "売上ファイル名が連番になっていません";
	private static final String INVALID_FORMAT = "のフォーマットが不正です";
	private static final String INVALID_BRANCH_CODE_ERROR = "の支店コードが不正です";
	private static final String INVALID_COMMODITY_CODE_ERROR = "の商品コードが不正です";
	private static final String MAX_SALES_AMOUNT_ERROR = "合計金額が10桁を超えました";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) throws IOException{

		// コマンドライン引数が渡されてるか確認
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();
		// 商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();
		// 商品コードと売上金額を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();

		// ファイルの存在チェック
		File branchFile = new File(args[0], "branch.lst");
		if(!branchFile.exists()) {
		    System.out.println(BRANCH_FILE_NOT_EXIST);
		    return;
		}

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales, "^[0-9]{3}$", BRANCH_FILE_INVALID_FORMAT)) {
			return;
		}

		// ファイルの存在チェック
		File commodityFile = new File(args[0], "commodity.lst");
		if(!commodityFile.exists()) {
		    System.out.println(COMMODITY_FILE_NOT_EXIST);
			    return;
		}

		// 商品定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_COMMODITY_LST, commodityNames, commoditySales, "[0-9a-zA-Z]{8}$", COMMODITY_FILE_INVALID_FORMAT)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		// 全てのファイルを取得
		File[] files = new File(args[0]).listFiles();


		// ファイル情報を格納するList
		List<File> rcdFiles = new ArrayList<>();

		// 8桁のファイル（売上ファイル）であればListに追加
		for(int i = 0; i < files.length; i++) {
			if(files[i].isFile() && files[i].getName().matches("^[0-9]{8}\\.rcd$")) {
				rcdFiles.add(files[i]);
			}
		}

		Collections.sort(rcdFiles);

		// 売上ファイルが連番か確認
		for(int i = 0; i < rcdFiles.size() -1; i++) {

			// 続く二つのファイル名の先頭8桁を取得しint型に変換
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			// 差が1でなければ連番になっていないのでエラー
			if((latter - former) != 1) {
				System.out.println(FILE_NAME_ORDER_ERROR);
				return;
			}
		}

		// 売上ファイルの数だけ繰り返す
		for(int i = 0; i < rcdFiles.size(); i++) {
			// ファイルを取り出す
			File file = rcdFiles.get(i);
			BufferedReader br = null;

			// ファイルの中身を読む
			try{
				br = new BufferedReader(new FileReader(file));

				//Listを作る
				List<String> salesLines = new ArrayList<>();

				String line;
				while ((line = br.readLine()) != null) {
					//作ったリストにaddする
					salesLines.add(line);
				}

				// 売上ファイルが3行になっているか確認
				if(salesLines.size() != 3) {
					System.out.println(rcdFiles.get(i).getName() + INVALID_FORMAT);
			        return;
				}

				// 変数にそれぞれ追加
				String branchCode = salesLines.get(0);
				String commodityCode = salesLines.get(1);
				String sales = salesLines.get(2);

				// 支店コードが存在しているかチェック
				if (!branchNames.containsKey(branchCode)) {
					System.out.println(rcdFiles.get(i).getName() + INVALID_BRANCH_CODE_ERROR);
			        return;
				}

				// 商品コードが存在しているかチェック
				if (!commodityNames.containsKey(commodityCode)) {
					System.out.println(rcdFiles.get(i).getName() + INVALID_COMMODITY_CODE_ERROR);
			        return;
				}

				// 数字かチェック
				if(!sales.matches("^[0-9]+$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				// 売上の型変換
				long fileSale = Long.parseLong(sales);

				// 支店売上の加算
				Long branchSaleAmount = branchSales.get(branchCode) + fileSale;

				// 商品売上の加算
				Long commoditySaleAmount = commoditySales.get(branchCode) + fileSale;

				// 合計売上金額が10桁を超えたかチェック
				if((branchSaleAmount >= 10000000000L) || (commoditySaleAmount >= 10000000000L)){
					System.out.println(MAX_SALES_AMOUNT_ERROR);
			        return;
				}

				// 加算した支店売上を追加
				branchSales.put(branchCode, branchSaleAmount);

				// 加算した商品売上を追加
				commoditySales.put(branchCode, commoditySaleAmount);

				// ファイルが開けない、読み込めない場合のエラー
			} catch (IOException e) {
		        System.out.println(UNKNOWN_ERROR);
		        return;
		    } finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales))  {
			return;
		}

		// 商品別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales))  {
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
	private static boolean readFile(String path, String fileName, Map<String, String> Names,
			Map<String, Long> Sales, String codeRegex, String errorMessage) {
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

				// フォーマット確認
				if((items.length != 2) || (!items[0].matches(codeRegex))){
				    System.out.println(errorMessage);
				    return false;
				}

				//Mapに追加する2つの情報をputの引数として指定
				Names.put(items[0], items[1]);
				Sales.put(items[0], 0L);

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
	private static boolean writeFile(String path, String fileName, Map<String, String> Names,
			Map<String, Long> Sales) throws IOException {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;
		// 書き出す準備
		try{
				bw = new BufferedWriter(new FileWriter(new File(path, fileName)));

			// 支店・商品コードを取り出してその分繰り返す
			for (String key : Names.keySet()) {

				// 支店コード、支店名、売上金額をファイルに書き込む
				bw.write(key + "," + Names.get(key) + "," + Sales.get(key));

				// 改行
				bw.newLine();
			}

		// ファイルが開けない、読み込めない場合のエラー
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}

		return true;
	}
}