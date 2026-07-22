import ExcelJS from 'exceljs';
import path from 'path';

export class ExcelReporter {
    constructor(reportName = 'e2e_web_report.xlsx') {
        this.workbook = new ExcelJS.Workbook();
        this.worksheet = this.workbook.addWorksheet('Test Results');
        this.reportPath = path.join(process.cwd(), 'tests', 'e2e_web', reportName);
        this.initializeHeaders();
    }

    initializeHeaders() {
        this.worksheet.columns = [
            { header: 'Test ID', key: 'id', width: 15 },
            { header: 'Category', key: 'category', width: 20 },
            { header: 'Role', key: 'role', width: 15 },
            { header: 'Action', key: 'action', width: 40 },
            { header: 'Expected', key: 'expectedOutcome', width: 20 },
            { header: 'Status', key: 'status', width: 15 },
            { header: 'Error', key: 'error', width: 30 },
            { header: 'Execution Time (ms)', key: 'duration', width: 20 },
            { header: 'Timestamp', key: 'timestamp', width: 25 },
        ];

        // Styling headers
        this.worksheet.getRow(1).font = { bold: true };
    }

    addRecord(testCase, status, error, duration) {
        this.worksheet.addRow({
            id: testCase.id,
            category: testCase.category,
            role: testCase.role,
            action: testCase.action,
            expectedOutcome: testCase.expectedOutcome,
            status: status,
            error: error ? error.toString() : '',
            duration: duration,
            timestamp: new Date().toISOString()
        });
    }

    async saveReport() {
        await this.workbook.xlsx.writeFile(this.reportPath);
        console.log(`Excel report saved successfully to: ${this.reportPath}`);
    }
}
