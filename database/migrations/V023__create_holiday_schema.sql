-- V023: Holiday Management Schema
-- 휴일 관리 스키마
-- Author: Moon Myung-seop
-- Date: 2026-01-25

-- ============================================================
-- Holidays Table
-- 휴일 테이블
-- ============================================================
CREATE TABLE IF NOT EXISTS common.holidays (
    holiday_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    holiday_name VARCHAR(200) NOT NULL,
    holiday_date DATE NOT NULL,

    -- Holiday classification
    holiday_type VARCHAR(50) NOT NULL, -- NATIONAL, COMPANY, SPECIAL
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_rule VARCHAR(100), -- YEARLY, MONTHLY, etc.

    -- Business day calculation
    is_working_day BOOLEAN DEFAULT FALSE, -- Some holidays may be working days

    description TEXT,
    remarks TEXT,

    -- Metadata
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_holiday_tenant FOREIGN KEY (tenant_id)
        REFERENCES common.tenants(tenant_id) ON DELETE CASCADE,
    CONSTRAINT uk_holiday_date UNIQUE (tenant_id, holiday_date)
);

COMMENT ON TABLE common.holidays IS '휴일 캘린더';
COMMENT ON COLUMN common.holidays.holiday_id IS '휴일 ID (PK)';
COMMENT ON COLUMN common.holidays.tenant_id IS '테넌트 ID (FK)';
COMMENT ON COLUMN common.holidays.holiday_name IS '휴일명';
COMMENT ON COLUMN common.holidays.holiday_date IS '휴일 날짜';
COMMENT ON COLUMN common.holidays.holiday_type IS '휴일 유형 (국경일/회사휴무/특별휴무)';
COMMENT ON COLUMN common.holidays.is_recurring IS '반복 휴일 여부';
COMMENT ON COLUMN common.holidays.recurrence_rule IS '반복 규칙';
COMMENT ON COLUMN common.holidays.is_working_day IS '근무일 여부 (대체 근무일 등)';

CREATE INDEX idx_holiday_tenant ON common.holidays(tenant_id);
CREATE INDEX idx_holiday_date ON common.holidays(holiday_date);
CREATE INDEX idx_holiday_type ON common.holidays(holiday_type);
CREATE INDEX idx_holiday_active ON common.holidays(is_active);
CREATE INDEX idx_holiday_date_range ON common.holidays(tenant_id, holiday_date);

-- ============================================================
-- Working Hours Table
-- 근무 시간 설정 테이블
-- ============================================================
CREATE TABLE IF NOT EXISTS common.working_hours (
    working_hours_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    schedule_name VARCHAR(100) NOT NULL,
    description TEXT,

    -- Day of week settings (0=Sunday, 1=Monday, ..., 6=Saturday)
    monday_start TIME,
    monday_end TIME,
    tuesday_start TIME,
    tuesday_end TIME,
    wednesday_start TIME,
    wednesday_end TIME,
    thursday_start TIME,
    thursday_end TIME,
    friday_start TIME,
    friday_end TIME,
    saturday_start TIME,
    saturday_end TIME,
    sunday_start TIME,
    sunday_end TIME,

    -- Break times
    break_start_1 TIME,
    break_end_1 TIME,
    break_start_2 TIME,
    break_end_2 TIME,

    -- Effective period
    effective_from DATE,
    effective_to DATE,

    -- Default schedule
    is_default BOOLEAN DEFAULT FALSE,

    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    CONSTRAINT fk_working_hours_tenant FOREIGN KEY (tenant_id)
        REFERENCES common.tenants(tenant_id) ON DELETE CASCADE
);

COMMENT ON TABLE common.working_hours IS '근무 시간 설정';
COMMENT ON COLUMN common.working_hours.working_hours_id IS '근무 시간 ID (PK)';
COMMENT ON COLUMN common.working_hours.tenant_id IS '테넌트 ID (FK)';
COMMENT ON COLUMN common.working_hours.schedule_name IS '스케줄 명칭';
COMMENT ON COLUMN common.working_hours.monday_start IS '월요일 시작 시간';
COMMENT ON COLUMN common.working_hours.monday_end IS '월요일 종료 시간';
COMMENT ON COLUMN common.working_hours.break_start_1 IS '1차 휴게 시작 시간';
COMMENT ON COLUMN common.working_hours.break_end_1 IS '1차 휴게 종료 시간';
COMMENT ON COLUMN common.working_hours.is_default IS '기본 스케줄 여부';

CREATE INDEX idx_working_hours_tenant ON common.working_hours(tenant_id);
CREATE INDEX idx_working_hours_default ON common.working_hours(is_default);
CREATE INDEX idx_working_hours_effective ON common.working_hours(tenant_id, effective_from, effective_to);

-- ============================================================
-- Sample Data: Korean National Holidays 2026
-- 샘플 데이터: 2026년 대한민국 공휴일
-- ============================================================
INSERT INTO common.holidays (tenant_id, holiday_name, holiday_date, holiday_type, is_recurring, recurrence_rule, is_working_day, description)
VALUES
    -- 2026년 공휴일
    ('DEFAULT_TENANT', '신정', '2026-01-01', 'NATIONAL', TRUE, 'YEARLY', FALSE, '새해 첫날'),
    ('DEFAULT_TENANT', '설날 연휴', '2026-02-16', 'NATIONAL', TRUE, 'LUNAR', FALSE, '설날 전날'),
    ('DEFAULT_TENANT', '설날', '2026-02-17', 'NATIONAL', TRUE, 'LUNAR', FALSE, '음력 1월 1일'),
    ('DEFAULT_TENANT', '설날 연휴', '2026-02-18', 'NATIONAL', TRUE, 'LUNAR', FALSE, '설날 다음날'),
    ('DEFAULT_TENANT', '삼일절', '2026-03-01', 'NATIONAL', TRUE, 'YEARLY', FALSE, '3.1 독립운동 기념일'),
    ('DEFAULT_TENANT', '어린이날', '2026-05-05', 'NATIONAL', TRUE, 'YEARLY', FALSE, '어린이날'),
    ('DEFAULT_TENANT', '부처님오신날', '2026-05-24', 'NATIONAL', TRUE, 'LUNAR', FALSE, '음력 4월 8일'),
    ('DEFAULT_TENANT', '현충일', '2026-06-06', 'NATIONAL', TRUE, 'YEARLY', FALSE, '현충일'),
    ('DEFAULT_TENANT', '광복절', '2026-08-15', 'NATIONAL', TRUE, 'YEARLY', FALSE, '광복절'),
    ('DEFAULT_TENANT', '추석 연휴', '2026-09-24', 'NATIONAL', TRUE, 'LUNAR', FALSE, '추석 전날'),
    ('DEFAULT_TENANT', '추석', '2026-09-25', 'NATIONAL', TRUE, 'LUNAR', FALSE, '음력 8월 15일'),
    ('DEFAULT_TENANT', '추석 연휴', '2026-09-26', 'NATIONAL', TRUE, 'LUNAR', FALSE, '추석 다음날'),
    ('DEFAULT_TENANT', '개천절', '2026-10-03', 'NATIONAL', TRUE, 'YEARLY', FALSE, '개천절'),
    ('DEFAULT_TENANT', '한글날', '2026-10-09', 'NATIONAL', TRUE, 'YEARLY', FALSE, '한글날'),
    ('DEFAULT_TENANT', '성탄절', '2026-12-25', 'NATIONAL', TRUE, 'YEARLY', FALSE, '크리스마스');

-- ============================================================
-- Sample Data: Default Working Hours
-- 샘플 데이터: 기본 근무 시간
-- ============================================================
INSERT INTO common.working_hours (
    tenant_id,
    schedule_name,
    description,
    monday_start, monday_end,
    tuesday_start, tuesday_end,
    wednesday_start, wednesday_end,
    thursday_start, thursday_end,
    friday_start, friday_end,
    saturday_start, saturday_end,
    sunday_start, sunday_end,
    break_start_1, break_end_1,
    effective_from,
    is_default,
    is_active
)
VALUES
    (
        'DEFAULT_TENANT',
        '표준 근무 시간',
        '주 5일 근무제 (월-금 09:00-18:00)',
        '09:00:00', '18:00:00',  -- Monday
        '09:00:00', '18:00:00',  -- Tuesday
        '09:00:00', '18:00:00',  -- Wednesday
        '09:00:00', '18:00:00',  -- Thursday
        '09:00:00', '18:00:00',  -- Friday
        NULL, NULL,              -- Saturday (no work)
        NULL, NULL,              -- Sunday (no work)
        '12:00:00', '13:00:00',  -- Lunch break
        '2026-01-01',
        TRUE,
        TRUE
    ),
    (
        'DEFAULT_TENANT',
        '3교대 - 주간',
        '3교대 주간 근무 (08:00-16:00)',
        '08:00:00', '16:00:00',
        '08:00:00', '16:00:00',
        '08:00:00', '16:00:00',
        '08:00:00', '16:00:00',
        '08:00:00', '16:00:00',
        '08:00:00', '16:00:00',
        '08:00:00', '16:00:00',
        '12:00:00', '13:00:00',
        '2026-01-01',
        FALSE,
        TRUE
    ),
    (
        'DEFAULT_TENANT',
        '3교대 - 중간',
        '3교대 중간 근무 (16:00-00:00)',
        '16:00:00', '23:59:59',
        '16:00:00', '23:59:59',
        '16:00:00', '23:59:59',
        '16:00:00', '23:59:59',
        '16:00:00', '23:59:59',
        '16:00:00', '23:59:59',
        '16:00:00', '23:59:59',
        '20:00:00', '21:00:00',
        '2026-01-01',
        FALSE,
        TRUE
    ),
    (
        'DEFAULT_TENANT',
        '3교대 - 야간',
        '3교대 야간 근무 (00:00-08:00)',
        '00:00:00', '08:00:00',
        '00:00:00', '08:00:00',
        '00:00:00', '08:00:00',
        '00:00:00', '08:00:00',
        '00:00:00', '08:00:00',
        '00:00:00', '08:00:00',
        '00:00:00', '08:00:00',
        '04:00:00', '05:00:00',
        '2026-01-01',
        FALSE,
        TRUE
    );
