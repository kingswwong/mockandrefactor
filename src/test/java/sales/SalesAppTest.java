package sales;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SalesAppTest {

	@InjectMocks
	private SalesApp salesApp;
	@Mock
	SalesReportDao salesReportDao;
	@Mock
	EcmService ecmService;

	@Test
	public void testGenerateReport() {

		SalesApp salesApp = spy(SalesApp.class);
		doReturn(true).when(salesApp).isDateValid(any());
		doReturn(new ArrayList<>()).when(salesApp).filteredReportDataList(any(), anyBoolean());
		doReturn(new ArrayList<>()).when(salesApp).generateTempList(any(), anyInt());
		doReturn(new SalesActivityReport()).when(salesApp).generateReport(any(), any());

		salesApp.generateSalesActivityReport("DUMMY", 1000, false, false);

		verify(salesApp, times(1)).generateReport(any(), any());
	}

	@Test
	public void should_return_false_when_validateDate_given_error_date() {
		SalesApp salesApp = spy(SalesApp.class);
		Sales sales = mock(Sales.class);
		when(sales.getEffectiveTo()).thenReturn(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
		when(sales.getEffectiveFrom()).thenReturn(new Date(System.currentTimeMillis() - 60 * 60 * 1000));

		boolean isValidDate = salesApp.validateDate(sales);

		assertTrue(isValidDate);
	}

	@Test
	public void should_return_list_when_filter_given_ReportDataAndIsSupervisor() {
		Sales sales = new Sales();
		SalesReportData salesReportData = mock(SalesReportData.class);
		when(salesReportData.getType()).thenReturn("SalesActivity");
		List<SalesReportData> reportDataList = Arrays.asList(new SalesReportData(), salesReportData);
		when(salesReportDao.getReportData(sales)).thenReturn(reportDataList);

		List<SalesReportData> filteredReportDataList = salesApp.filteredReportDataList(reportDataList, true);

		Assert.assertEquals(1, filteredReportDataList.size());
		Assert.assertEquals("SalesActivity", filteredReportDataList.get(0).getType());
	}

	@Test
	public void should_return_nothing_when_filter_givenReportDataAndIsNotSupervisor() {
		Sales sales = new Sales();
		SalesReportData salesReportData = mock(SalesReportData.class);
		when(salesReportData.getType()).thenReturn("SalesActivity");
		when(salesReportData.isConfidential()).thenReturn(true);
		List<SalesReportData> reportDataList = Arrays.asList(new SalesReportData(), salesReportData);
		when(salesReportDao.getReportData(sales)).thenReturn(reportDataList);

		List<SalesReportData> filteredReportDataList = salesApp.filteredReportDataList(reportDataList, false);

		Assert.assertEquals(0, filteredReportDataList.size());
	}

	@Test
	public void should_return_templist_when_generateTempList_givenThreeReportDataAndMaxRowIs4() {
		Sales sales = new Sales();
		List<SalesReportData> reportDataList = Arrays.asList(new SalesReportData(), new SalesReportData(), new SalesReportData());
		when(salesReportDao.getReportData(sales)).thenReturn(reportDataList);

		List<SalesReportData> filteredReportDataList = salesApp.generateTempList(reportDataList, 4);

		Assert.assertEquals(3, filteredReportDataList.size());
	}

	@Test
	public void should_return_templist_when_generateTempList_givenThreeReportDataAndMaxRowIs2() {
		List<SalesReportData> reportDataList = Arrays.asList(new SalesReportData(), new SalesReportData(), new SalesReportData());

		List<SalesReportData> filteredReportDataList = salesApp.generateTempList(reportDataList, 2);

		Assert.assertEquals(2, filteredReportDataList.size());
	}

	@Test
	public void should_return_local_time_when_createHeaders_given_false() {
		SalesApp salesApp = new SalesApp();
		boolean isNatTrade = false;

		List<String> headers = salesApp.createHeader(isNatTrade);

		Assert.assertTrue(headers.contains("Local Time"));
	}

	@Test
	public void should_return_time_when_createHeaders_given_true() {
		SalesApp salesApp = new SalesApp();
		boolean isNatTrade = true;

		List<String> headers = salesApp.createHeader(isNatTrade);

		Assert.assertTrue(headers.contains("Time"));
	}

	@Test
	public void testTransferReportToXml_givenSalesActivityReport_thenInvokeUploadDocument() {
		SalesActivityReport spyActivityReport = mock(SalesActivityReport.class);

		salesApp.upload(spyActivityReport);

		verify(ecmService, times(1)).uploadDocument(any());
		verify(spyActivityReport, times(1)).toXml();
	}
}
