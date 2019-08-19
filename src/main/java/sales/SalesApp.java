package sales;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SalesApp {

	private SalesDao salesDao;
	private SalesReportDao salesReportDao;
	private EcmService ecmService;

	public SalesApp() {
		this.salesDao = new SalesDao();
		this.salesReportDao = new SalesReportDao();
		this.ecmService = new EcmService();
	}

	public Sales getSalesBySalesId(String salesId){
		Sales sales = salesDao.getSalesBySalesId(salesId);
		return sales;
	}

	protected boolean isDateValid(Sales sales) {
		Date today = new Date();
		return !today.after(sales.getEffectiveTo()) && !today.before(sales.getEffectiveFrom());
	}


	protected void upload(SalesActivityReport report) {
		ecmService.uploadDocument(report.toXml());
	}

	protected List<String> createHeader(boolean isNatTrade) {
		List<String> headers = new ArrayList<>();
		if (isNatTrade) {
			headers = Arrays.asList("Sales ID", "Sales Name", "Activity", "Time");
		} else {
			headers = Arrays.asList("Sales ID", "Sales Name", "Activity", "Local Time");
		}
		return headers;
	}


	protected boolean validateDate(Sales sales) {
		Date today = new Date();
		return !today.after(sales.getEffectiveTo()) && !today.before(sales.getEffectiveFrom());
	}

	public List<SalesReportData> filteredReportDataList(List<SalesReportData> reportDataList, boolean isSupervisor) {
		List<SalesReportData> result = new ArrayList<>();
		for (SalesReportData data : reportDataList) {
			if ("SalesActivity".equalsIgnoreCase(data.getType())) {
				if (data.isConfidential()) {
					if (isSupervisor) {
						result.add(data);
					}
				} else {
					result.add(data);
				}
			}
		}
		return result;
	}

	public List<SalesReportData> generateTempList(List<SalesReportData> reportDataList, int maxRow) {
		List<SalesReportData> tempList = new ArrayList<>();
		for (int i = 0; i < reportDataList.size() && i < maxRow; i++) {
			tempList.add(reportDataList.get(i));
		}
		return tempList;
	}

	public void generateSalesActivityReport(String salesId, int maxRow, boolean isNatTrade, boolean isSupervisor) {
		if (salesId == null) {
			return;
		}

		Sales sales = getSalesBySalesId(salesId);

		if (!isDateValid(sales)) {
			return;
		}

		List<SalesReportData> reportDataList = salesReportDao.getReportData(sales);

		List<SalesReportData> filteredReportDataList = filteredReportDataList(reportDataList, isSupervisor);

		filteredReportDataList = generateTempList(reportDataList, maxRow);

		List<String> headers = createHeader(isNatTrade);

		SalesActivityReport report = this.generateReport(headers, reportDataList);

		upload(report);

	}

	public SalesActivityReport generateReport(List<String> headers, List<SalesReportData> reportDataList) {
		// TODO Auto-generated method stub
		return new SalesActivityReport();
	}

}
