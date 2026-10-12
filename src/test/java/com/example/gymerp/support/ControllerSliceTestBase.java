package com.example.gymerp.support;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.gymerp.config.FileStorageProperties;
import com.example.gymerp.repository.CategoryDao;
import com.example.gymerp.repository.DashboardDao;
import com.example.gymerp.repository.EmpAttendanceDao;
import com.example.gymerp.repository.EmpDao;
import com.example.gymerp.repository.LogDao;
import com.example.gymerp.repository.MemberDao;
import com.example.gymerp.repository.ModalDao;
import com.example.gymerp.repository.PostDao;
import com.example.gymerp.repository.ProductDao;
import com.example.gymerp.repository.PtRegistrationMapper;
import com.example.gymerp.repository.SalesAnalyticsDao;
import com.example.gymerp.repository.SalesItemDao;
import com.example.gymerp.repository.SalesServiceDao;
import com.example.gymerp.repository.ScheduleDao;
import com.example.gymerp.repository.ServiceDao;
import com.example.gymerp.repository.StockDao;

/**
 * 공용 컨트롤러 슬라이스 테스트 베이스.
 * MyBatis {@code @MapperScan}이 등록하려는 DAO/Mapper 들을 모두 MockBean으로 덮어서
 * WebMvcTest 컨텍스트가 SqlSessionFactory 없이도 기동될 수 있도록 한다.
 * WebConfig(정적 리소스 핸들러)가 의존하는 FileStorageProperties 도 함께 등록한다.
 */
@Import(FileStorageProperties.class)
public abstract class ControllerSliceTestBase {

    @MockitoBean protected CategoryDao categoryDao;
    @MockitoBean protected DashboardDao dashboardDao;
    @MockitoBean protected EmpAttendanceDao empAttendanceDao;
    @MockitoBean protected EmpDao empDao;
    @MockitoBean protected LogDao logDao;
    @MockitoBean protected MemberDao memberDao;
    @MockitoBean protected ModalDao modalDao;
    @MockitoBean protected PostDao postDao;
    @MockitoBean protected ProductDao productDao;
    @MockitoBean protected PtRegistrationMapper ptRegistrationMapper;
    @MockitoBean protected SalesAnalyticsDao salesAnalyticsDao;
    @MockitoBean protected SalesItemDao salesItemDao;
    @MockitoBean protected SalesServiceDao salesServiceDao;
    @MockitoBean protected ScheduleDao scheduleDao;
    @MockitoBean protected ServiceDao serviceDao;
    @MockitoBean protected StockDao stockDao;
}
