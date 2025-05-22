# 卡牌遊戲專案完善計畫 To-Do List

## I. 🚫 安全性強化 (Security Hardening) - 最高優先級

-   [ ] **密碼加密儲存**:
    -   [ ] 研究並選擇合適的密碼雜湊演算法 (如 bcrypt, scrypt, Argon2)。
    -   [ ] 修改 `GameRecordService.registerUser` 方法，對使用者密碼進行加鹽雜湊後再存入資料庫。
    -   [ ] 修改 `GameRecordService.loginUser` 方法，驗證時對輸入密碼進行同樣的加鹽雜湊，再與資料庫中儲存的雜湊值比對。
    -   [ ] 處理現有已註冊使用者的密碼遷移問題（例如，在使用者下次登入時更新其密碼儲存方式）。
    -   [ ] 移除 `GameRecordService` 構造函數中硬編碼的 `admin`/`admin` 帳號，或使其密碼也被雜湊儲存。考慮首次啟動引導設定管理員。
-   [ ] **預設管理員帳號安全**:
    -   [ ] 移除或更改預設的 'admin'/'admin' 帳號和密碼。
    -   [ ] 考慮首次啟動時引導使用者設定管理員帳號和密碼。

## II. 🏗️ 程式碼結構與設計 (Code Structure & Design)

-   [ ] **強化 MVC/MVVM 模式**:
    -   [ ] 將 `GameGUI` 中直接呼叫 `recordService` 的邏輯移至 `GameController`。
    -   [ ] `GameController` 應處理來自 `GameGUI` 的使用者操作請求，並調用相應的服務。
-   [ ] **`GameRecordService` 改進**:
    -   [ ] **SQL 常數化**: 將資料庫表名、欄位名定義為 `public static final String` 常數。
    -   [ ] **Schema 管理**:
        -   [ ] 研究 Flyway 或 Liquibase 等資料庫遷移工具，評估是否引入。
        -   [ ] (短期) 改善 `ALTER TABLE` 邏輯，例如先查詢 `PRAGMA table_info(table_name);` 確認欄位是否存在再執行 `ALTER TABLE`，而不是依賴 `try-catch (SQLException ignored)`。
    -   [ ] **錯誤處理**: 將 `e.printStackTrace()` 替換為日誌記錄，並考慮向使用者顯示更友好的錯誤訊息。
-   [ ] **`GameGUI` 模組化**:
    -   [ ] 考慮將 `loginPanel`, `lobbyPanel`, `drawOptionsPanel`, `battlePanel`, `selectionPanel`, `rankingPanel` 等拆分為獨立的 `JPanel` 子類別。
-   [ ] **常數管理**:
    -   [ ] 將 `CardLayout` 的面板名稱定義為 `GameGUI` 內的靜態常數。
    -   [ ] 遊戲預設數值（初始貨幣、評分等）若多處使用，應定義為常數或從設定檔讀取。
-   [ ] **設定檔**:
    -   [ ] 考慮將資料庫檔案名稱 (`DB_FILENAME`)、預設遊戲參數等移至外部設定檔 (如 `.properties` 檔案)。

## III. 🎨 使用者介面與體驗 (GUI & UX)

-   [ ] **UI 響應性**:
    -   [ ] 識別 `GameGUI` 中可能導致 UI 卡頓的長時操作（如登入、註冊、抽卡、開始對戰、儲存記錄）。
    -   [ ] 使用 `SwingWorker` 將這些長時操作移至背景執行緒，並在完成後更新 UI。
-   [ ] **輸入驗證**:
    -   [ ] 增強使用者名稱和密碼的輸入驗證邏輯（如最小/最大長度、允許字元等）。
-   [ ] **抽卡動畫**:
    -   [ ] 確保 `showAnimationEffect("card_draw")` 的實現是流暢且非阻塞的。
-   [ ] **檢視牌組 (`showDeck`)**:
    -   [ ] 設計並實現一個清晰、美觀的介面來展示玩家擁有的卡牌及其詳細資訊。
    -   [ ] 考慮卡牌排序、篩選功能。
-   [ ] **排行榜 (`rankingPanel`)**:
    -   [ ] 確保 `CustomRankingRenderer` 能良好展示玩家資訊。
    -   [ ] 考慮增加更多排行榜類型或篩選條件。
-   [ ] **主題與樣式**:
    -   [ ] 將 `GameGUI` 中的顏色、字型等樣式元素提取到一個集中的地方或主題管理類別。
    -   [ ] 允許使用者切換不同主題（若 `isDarkTheme` 相關邏輯已完整）。
-   [ ] **(選用) 國際化 (i18n)**:
    -   [ ] 若有計劃支援多語言，將 UI 字串提取到 `ResourceBundle`。

## IV. 🎮 遊戲邏輯 (Game Logic)

-   [ ] **電腦 AI 增強**:
    -   [ ] 設計並實現更具策略性的電腦出牌 AI，而不僅僅是選擇第一張牌。
-   [ ] **遊戲平衡性調整**:
    -   [ ] 審查卡牌的屬性、稀有度、技能效果，確保遊戲平衡。
    -   [ ] 根據測試回饋調整玩家升級、獲取貨幣、評分變動的數值。
-   [ ] **(選用) 新功能/擴展**:
    -   [ ] 考慮新增卡牌技能、特殊效果。
    -   [ ] 考慮新增不同的遊戲模式。

## V. 🛠️ 建置、部署與測試 (Build, Deployment & Testing)

-   [ ] **引入建置工具 (Maven/Gradle)**:
    -   [ ] 初始化 Maven 或 Gradle 專案結構。
    -   [ ] 將 `sqlite-jdbc.jar` 等依賴項加入建置設定檔。
    -   [ ] 設定建置工具以產生包含所有依賴的可執行 JAR。
-   [ ] **單元測試 (JUnit)**:
    -   [ ] 為 `GameController` 的核心邏輯編寫單元測試。
    -   [ ] 為 `BattleService`, `GachaService` 編寫單元測試。
    -   [ ] 為 `GameRecordService` 中不直接涉及資料庫的輔助方法編寫單元測試。
-   [ ] **整合測試**:
    -   [ ] 編寫測試驗證 `GameRecordService` 與資料庫的互動是否正確（增刪改查）。
    -   [ ] 測試使用者註冊、登入、儲存遊戲記錄等完整流程。
-   [ ] **日誌系統**:
    -   [ ] 引入 SLF4j + Logback (或 Log4j2) 作為日誌框架。
    -   [ ] 替換專案中所有的 `System.out.println` 和 `System.err.println` 為日誌框架的呼叫。
    -   [ ] 設定日誌級別和輸出目標（如控制台和日誌檔案）。

## VI. 📄 文件 (Documentation)

-   [ ] **`README.md` 完善**:
    -   [ ] 新增專案簡介。
    -   [ ] 新增開發環境設定步驟。
    -   [ ] 新增如何編譯和執行專案的說明 (若使用 Maven/Gradle，則說明相應指令)。
    -   [ ] 概述主要功能和遊戲玩法。
-   [ ] **程式碼註解**:
    -   [ ] 檢查並更新現有的 JavaDoc 和行內註解，確保其準確性和完整性。
    -   [ ] 為新增的類別和方法添加 JavaDoc。

## VII. ✨ 其他建議 (Miscellaneous)

-   [ ] **資源管理**:
    -   [ ] 檢查是否有其他未關閉的資源 (雖然 Swing 元件由框架管理，但若有自訂的檔案讀寫等需注意)。
-   [ ] **效能分析**:
    -   [ ] 在進行了較多修改後，可使用 Profiler (如 VisualVM) 分析應用程式效能瓶頸。

---

**執行順序建議:**

1.  **安全性強化**：這是首要任務，尤其是密碼加密。
2.  **引入建置工具**：這有助於後續的依賴管理和打包。
3.  **日誌系統**：儘早引入，方便後續開發和調試。
4.  **程式碼結構與設計**：逐步重構，先從 `GameGUI` 的邏輯分離開始。
5.  **UI 響應性**：解決卡頓問題，提升使用者體驗。
6.  **測試**：邊重構邊補充測試案例。
7.  其他項目可根據實際情況和優先級穿插進行。

祝您的專案越來越完善！
