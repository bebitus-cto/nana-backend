package com.nana.nana.domain.enums

import com.nana.nana.domain.feature.translation.config.Translatable

enum class Party(
    override val koValue: String,
    override val enValue: String,
    override val zhValue: String,
    override val jaValue: String
) : Translatable {

    YUSHIN_JUNGWOO(
        "유신정우회",
        "Yushin Jeongwohoe",
        "维新正友会",
        "維新正友会"
    ),
    JOGUK_HYEOKSIN(
        "조국혁신당",
        "Patriotic Reform Party",
        "祖国革新党",
        "祖国革新党"
    ),
    MINJU_JEONGUI(
        "민주정의당",
        "Democratic Justice Party",
        "民主正义党",
        "民主正義党"
    ),
    MINJU_JAYU(
        "민주자유당",
        "Democratic Liberal Party",
        "民主自由党",
        "民主自由党"
    ),
    SHIN_HANGUK(
        "신한국당",
        "New Korea Party",
        "新韩国党",
        "新韓国党"
    ),
    HANGUK_GUKMIN(
        "한국국민당",
        "Korea National Party",
        "韩国国民党",
        "韓国国民党"
    ),
    INDEPENDENT(
        "무소속",
        "Independent",
        "无党派",
        "無所属"
    ),
    JAYU(
        "자유당",
        "Liberal Party",
        "自由党",
        "自由党"
    ),
    SHIN_MIN(
        "신민당",
        "New Democratic Party",
        "新民主党",
        "新民主党"
    ),
    PYEONGHWA_MINJU(
        "평화민주당",
        "Peace Democratic Party",
        "和平民主党",
        "平和民主党"
    ),
    MINJU_NODONG(
        "민주노동당",
        "Democratic Labor Party",
        "民主劳动党",
        "民主労働党"
    ),
    SAENURI(
        "새누리당",
        "Saenuri Party",
        "新世界党",
        "セヌリ党"
    ),
    MIRAE_TONGHAP(
        "미래통합당",
        "United Future Party",
        "未来统合党",
        "未来統合党"
    ),
    YEOLLIN_URI(
        "열린우리당",
        "Our Open Party",
        "开放我们党",
        "オープンウリ党"
    ),
    TONGHAP_MINJU(
        "통합민주당",
        "United Democratic Party",
        "统一民主党",
        "統合民主党"
    ),
    MINJU_TONGHAP(
        "민주통합당",
        "Democratic United Party",
        "民主统合党",
        "民主統合党"
    ),
    MINJU_GONGHWA(
        "민주공화당",
        "Democratic Republican Party",
        "民主共和党",
        "民主共和党"
    ),
    BUSAN_15GURAK(
        "부산15구락부",
        "Busan 15 Club",
        "釜山15俱乐部",
        "釜山15クラブ"
    ),
    GUKMIN_EUI_HIM(
        "국민의힘",
        "People Power Party",
        "国民之力",
        "国民の力"
    ),
    TONGHAP_JINBO(
        "통합진보당",
        "Unified Progressive Party",
        "统一进步党",
        "統合進歩党"
    ),
    DEO_MINJU(
        "더불어민주당",
        "Democratic Party",
        "共同民主党",
        "共に民主党"
    ),
    HAN_NARA(
        "한나라당",
        "Grand National Party",
        "大国家党",
        "ハンナラ党"
    ),
    MINJEONG(
        "민정당",
        "Democratic Justice Party",
        "民政党",
        "民政党"
    ),
    YEOLLIN_MINJU(
        "열린민주당",
        "Open Democratic Party",
        "开放民主党",
        "オープン民主党"
    ),
    MINJU_HANGUK(
        "민주한국당",
        "Democratic Korea Party",
        "民主韩国党",
        "民主韓国党"
    ),
    TONGIL_MINJU(
        "통일민주당",
        "Democratic Unification Party",
        "民主统一党",
        "民主統一党"
    ),
    SAE_CHEONNYEON_MINJU(
        "새천년민주당",
        "New Millennium Democratic Party",
        "新千年民主党",
        "新千年民主党"
    ),
    MINJU(
        "민주당",
        "Democratic Party",
        "民主党",
        "民主党"
    ),
    TONGIL_GUKMIN(
        "통일국민당",
        "Unification National Party",
        "统一国民党",
        "統一国民党"
    ),
    SHIN_HAN_MINJU(
        "신한민주당",
        "New Korean Democratic Party",
        "新韩民主党",
        "新韓民主党"
    ),
    GUKMIN_EUI_MIRAE(
        "국민의미래",
        "Future of People",
        "国民的未来",
        "国民の未来"
    ),
    JINBO(
        "진보당",
        "Progressive Party",
        "进步党",
        "進歩党"
    ),
    MINJU_GUKMIN(
        "민주국민당",
        "Democratic National Party",
        "民主国民党",
        "民主国民党"
    ),
    JOSEON_MINJOK_CHEONGNYEON(
        "조선민족청년단",
        "Korean National Youth Corps",
        "朝鲜民族青年团",
        "朝鮮民族青年団"
    ),
    DEO_MINJU_YEONHAP(
        "더불어민주연합",
        "Democratic Alliance",
        "民主联合",
        "民主連合"
    ),
    JEONGUI(
        "정의당", "Justice Party",
        "正义党",
        "正義党"
    ),
    JAYU_MINJU_YEONHAP(
        "자유민주연합",
        "Liberal Democratic Coalition",
        "自由民主联合",
        "自由民主連合"
    ),
    MINJUNG(
        "민중당",
        "People’s Party",
        "民众党",
        "民衆党"
    ),
    MINJU_SAHOE(
        "민주사회당",
        "Democratic Socialist Party",
        "民主社会党", "民主社会党"
    ),
    DAEHAN_DOKRIP_CHOKSEONG(
        "대한독립촉성국민회",
        "National Association for Korean Independence",
        "大韩独立促成国民会",
        "大韓独立促成国民会"
    ),
    SHIN_MINJU_GONGHWA(
        "신민주공화당",
        "New Democratic Republican Party",
        "新民主共和党",
        "新民主共和党"
    ),
    SAE_JEONGCHI_GUKMIN_HOEUI(
        "새정치국민회의",
        "New Political National Assembly",
        "新政治国民会议",
        "新政治国民会議"
    ),
    ILMIN_GURAK(
        "일민국락부",
        "One People Club",
        "一民族俱乐部",
        "一民族クラブ"
    ),
    JAYU_SEONJIN(
        "자유선진당",
        "Liberty Forward Party",
        "自由先进党",
        "自由先進党"
    ),
    GUKMIN_HOE(
        "국민회",
        "National Assembly",
        "国民会",
        "国民会"
    ),
    SAE_JEONGCHI_MINJU_YEONHAP(
        "새정치민주연합",
        "New Political Democratic Alliance",
        "新政治民主联合",
        "新政治民主連合"
    ),
    GUKMIN_EUI_DANG(
        "국민의당",
        "People’s Party",
        "国民之党",
        "国民の党"
    ),
    DEO_BUREO_SI_MIN(
        "더불어시민당",
        "Together Citizen Party",
        "共同行政党",
        "共同行政党"
    ),
    DAEHAN_GUKMIN(
        "대한국민당",
        "Great Korea National Party",
        "大韩国民党",
        "大韓国民党"
    ),
    MINJU_TONGIL(
        "민주통일당",
        "Democratic Unification Party",
        "民主统一党",
        "民主統一党"
    ),
    DAE_DONG_CHEONGNYEON(
        "대동청년당",
        "Daedong Youth Party",
        "大同青年党",
        "大同青年党"
    ),
    CHIN_BAK_YEONDAE(
        "친박연대",
        "Pro-Park Alliance",
        "亲朴联盟",
        "親朴連盟"
    ),
    HANGUK_MINJU(
        "한국민주당",
        "Korean Democratic Party",
        "韩国民主党",
        "韓国民主党"
    ),
    JAYU_MINJU(
        "자유민주당",
        "Liberal Democratic Party",
        "自由民主党",
        "自由民主党"
    ),
    HANGUK_DOKRIP(
        "한국독립당",
        "Korean Independence Party",
        "韩国独立党",
        "韓国独立党"
    ),
    DAEHAN_CHEONGNYEON(
        "대한청년단",
        "Korean Youth Corps",
        "大韩青年团",
        "大韓青年団"
    ),
    GYUYUK_HYUPHOE(
        "교육협회",
        "Education Association",
        "教育协会",
        "教育協会"
    ),
    MINJOK_TONGIL_BONBU(
        "민족통일본부",
        "National Reunification Headquarters",
        "民族统一本部",
        "民族統一本部"
    ),
    HANGUK_SAHOE(
        "한국사회당",
        "Korean Socialist Party",
        "韩国社会党",
        "韓国社会党"
    ),
    JOSEON_GONGHWA(
        "조선공화당",
        "Joseon Republican Party",
        "朝鲜共和党",
        "朝鮮共和党"
    ),
    DAE_TONGHAP_MINJU_SHINDANG(
        "대통합민주신당",
        "Great United Democratic New Party",
        "大统合民主新党",
        "大統合民主新党"
    ),
    MIRAE_HANGUK(
        "미래한국당",
        "Future Korea Party",
        "未来韩国党",
        "未来韓国党"
    ),
    HANGUK_SHINDANG(
        "한국신당",
        "Korea New Party",
        "韩国新党",
        "韓国新党"
    ),
    JEONDO_HOE(
        "전도회",
        "Evangelical Association",
        "传道会",
        "伝道会"
    ),
    AEGOOK_DANCHAE_YEONHAP(
        "애국단체연합회",
        "Patriotic Organizations Federation",
        "爱国团体联合会",
        "愛国団体連合会"
    ),
    JAYU_HANGUK(
        "자유한국당",
        "Liberty Korea Party",
        "自由韩国党",
        "自由韓国党"
    ),
    MINGWON(
        "민권당",
        "Civil Rights Party",
        "民权党",
        "民権党"
    ),
    SAE_LOUN_MIRAE(
        "새로운미래",
        "New Future",
        "新未来",
        "新未来"
    ),
    TONGIL(
        "통일당",
        "Unification Party",
        "统一党",
        "統一党"
    ),
    DAEHAN_NODONG(
        "대한노동총연맹",
        "Korean Federation of Labor",
        "大韩劳动总联盟",
        "大韓労働総連盟"
    ),
    CHANGJO_HANGUK(
        "창조한국당",
        "Creative Korea Party",
        "创造韩国党",
        "創造韓国党"
    ),
    SAHOE_DAEJUNG(
        "사회대중당",
        "Social Mass Party",
        "社会大众党",
        "社会大衆党"
    ),
    JEHEON_GUKHOE_HOHON(
        "제헌국회호헌동지회",
        "Constitutional Assembly Guardians Association",
        "制宪国会护宪同志会",
        "制憲国会護憲同志会"
    ),
    JUNGGANG_BULGYO(
        "중앙불교위원회",
        "Central Buddhist Committee",
        "中央佛教委员会",
        "中央仏教委員会"
    ),
    DAEHAN_BUIN(
        "대한부인회",
        "Korean Women's Association",
        "大韩妇人会",
        "大韓婦人会"
    ),
    GUKMIN_DANG(
        "국민당",
        "National Party",
        "国民党",
        "国民党"
    ),
    SHIN_JEONGCHI_GAERYUK(
        "신정치개혁당",
        "New Political Reform Party",
        "新政治改革党",
        "新政治改革党"
    ),
    HAN_GYERAE_MINJU(
        "한겨레민주당",
        "Hankyoreh Democratic Party",
        "韩民族民主党",
        "ハンギョレ民主党"
    ),
    SHIN_JEONG_SAHOE(
        "신정사회당",
        "New Political Social Party",
        "新政治社会党",
        "新政治社会党"
    ),
    DAEJUNG(
        "대중당",
        "Mass Party",
        "大众党",
        "大衆党"
    ),
    JAYU_MINJOK(
        "자유민족당",
        "Liberal National Party",
        "自由民族党",
        "自由民族党"
    ),
    GUKMIN_JUNGSHIM(
        "국민중심당",
        "People-Centered Party",
        "以民为本党",
        "国民中心党"
    ),
    GIBON_SODEUK(
        "기본소득당",
        "Basic Income Party",
        "基本收入党",
        "基本所得党"
    ),
    MINJU_JAJU_YEONMAENG(
        "민주자주연맹",
        "Democratic Autonomy Alliance",
        "民主自立联盟",
        "民主自立連合"
    ),
    DANMIN(
        "단민당",
        "Danmin Party",
        "丹民党",
        "ダンミン党"
    ),
    JEONGMIN_HOE(
        "정민회",
        "Justice People’s Association",
        "正民会",
        "正民会"
    ),
    GAEHYUK_GUKMIN(
        "개혁국민정당",
        "Reform National Party",
        "改革国民正党",
        "改革国民正党"
    ),
    MINJU_GUPA_DONGJIHOE(
        "민주당구파동지회",
        "Democratic Party Faction Branch",
        "民主党派支部",
        "民主党派支部"
    ),
    GEUNRO_NONGMIN(
        "근로농민당",
        "Workers and Farmers Party",
        "劳动农民党",
        "労働農民党"
    ),
    SHIN_JEONGDANG(
        "신정당",
        "New Justice Party",
        "新正党",
        "新正党"
    ),
    DAEHAN_DOKRIP_NONGMIN(
        "대한독립촉성농민총연맹",
        "Korean Independent Farmers Federation",
        "大韩独立促成农民总联盟",
        "大韓独立促成農民総連盟"
    ),
    JOSEON_MINJU(
        "조선민주당",
        "Joseon Democratic Party",
        "朝鲜民主党",
        "朝鮮民主党"
    ),
    GAEHYUK_SHINDANG(
        "개혁신당",
        "Reform New Party",
        "改革新党",
        "改革新党"
    ),
    DAEHAN_YEOJA_GUKMIN(
        "대한여자국민당",
        "Korean Women’s National Party",
        "大韩女子国民党",
        "大韓女子国民党"
    ),
    BAREUN_MIRAE(
        "바른미래당",
        "Bareun Mirae Party",
        "正未来党",
        "正未来党"
    ),
    GUKMIN_TONGHAP21(
        "국민통합21",
        "National Integration 21",
        "国民统合21",
        "国民統合21"
    ),
    HYUKSIN_DONGWOO(
        "혁신동우회",
        "Innovation Friends Association",
        "革新同友会",
        "革新同友会"
    ),
    SAHOE(
        "사회당",
        "Socialist Party",
        "社会党",
        "社会党"
    ),
    JINBO_SHINDANG(
        "진보신당",
        "Progressive New Party",
        "进步新党",
        "進歩新党"
    ),
    SHIDAE_JEONHWAN(
        "시대전환",
        "Era Transition",
        "时代转换",
        "時代転換"
    ),
    HEONJEONG_DONGWOO(
        "헌정동우회",
        "Constitutional Friends Association",
        "宪政同友会",
        "憲政同友会"
    ),
    KJUNGCHI_HYUKSIN(
        "K정치혁신연합당",
        "K Political Innovation Union Party",
        "K政治创新联盟党",
        "K政治革新連合党"
    ),
    GAGA_KUKMIN_CHAMYEOSINDANG(
        "가가국민참여신당",
        "Gaga People's Participation New Party",
        "Gaga国民参与新党",
        "ガガ国民参加新党"
    ),
    GAGA_HOHO_GONGMYEONG_SUNGEO_DAEDANG(
        "가가호호공명선거대한당",
        "Gaga Hoho Concord Electoral Grand Party",
        "Gaga浩浩共鸣选举大党",
        "ガガホホ共鳴選挙大党"
    ),
    GANA_BAN_GONGJEONGDANG_KOREA(
        "가나반공정당코리아",
        "Gana Anti-Communist Party Korea",
        "Gana反共正党韩国",
        "ガナ反共正党韓国"
    ),
    GARAK_TEUKGWEON_PYEJI_DANG(
        "가락특권폐지당",
        "Garak Abolition of Privileges Party",
        "가락特权废止党",
        "ガラク特権廃止党"
    ),
    GUKGA_HYEOKMYEONG_DANG(
        "국가혁명당",
        "National Revolutionary Party",
        "国家革命党",
        "国家革命党"
    ),
    GUKMIN_DAE_TONGHAP_DANG(
        "국민대통합당",
        "People's Grand Unification Party",
        "国民大统一党",
        "国民大統一党"
    ),
    DAEHAN_MINGUK_DANG(
        "대한민국당",
        "Republic of Korea Party",
        "大韩民国党",
        "大韓民国党"
    ),
    DAEHAN_SANGGONGIN_DANG(
        "대한상공인당",
        "Korean Federation of Business Party",
        "大韩商工人党",
        "大韓商工人党"
    ),
    DAEJUNG_DEMOCRATIC_PARTY(
        "대중민주당",
        "Mass Democracy Party",
        "大众民主党",
        "大衆民主党"
    ),
    HALLYU_YEONHAP_DANG(
        "한류연합당",
        "Korean Wave Union Party",
        "韩流联合党",
        "韓流連合党"
    ),
    HONGIK_DANG(
        "홍익당",
        "Hongik Party",
        "弘益党",
        "弘益党"
    ),
    HISI_TAG_GUKMIN_JEONGCHAEK_DANG(
        "히시태그국민정책당",
        "#National Policy Party",
        "#国民政策党",
        "#国民政策党"
    ),
    JAYU_MINJU_YEONHAP2(
        "자유민족연합",
        "Liberal National Coalition",
        "自由民族联盟",
        "自由民族連合"
    ),
    JAYU_JUNGCHI_DANG(
        "자유정치당",
        "Liberal Political Party",
        "自由政治党",
        "自由政治党"
    ),
    MINJU_SAHOE_YEONHAP(
        "민주사회연합",
        "Democratic Social Alliance",
        "民主社会联盟",
        "民主社会連合"
    ),
    GUKMIN_JAYU_DANG(
        "국민자유당",
        "People's Liberal Party",
        "国民自由党",
        "国民自由党"
    ),
    JAYU_NODONG_DANG(
        "자유노동당",
        "Liberal Labor Party",
        "自由劳动党",
        "自由労働党"
    ),
    GUKMIN_HYEOKSIN_DANG(
        "국민혁신당",
        "National Innovation Party",
        "国民创新党",
        "国民革新党"
    ),
    JINBO_GUKMIN_DANG(
        "진보국민당",
        "Progressive National Party",
        "进步国民党",
        "進歩国民党"
    ),
    GUKMIN_MINJU_DANG(
        "국민민주당",
        "National Democratic Party",
        "国民民主党",
        "国民民主党"
    ),
    JAYU_DAE_TONGHAP_DANG(
        "자유대통합당",
        "Liberal Grand Unification Party",
        "自由大统一党",
        "自由大統一党"
    ),
    MINJU_JINBO_YEONHAP(
        "민주진보연합",
        "Democratic Progressive Alliance",
        "民主进步联盟",
        "民主進歩連合"
    ),
    MINJOK_MINJU_YEONHAP(
        "민족민주연합",
        "National Democratic Alliance",
        "民族民主联盟",
        "民族民主連合"
    ),
    GUKMIN_BALJEONDANG(
        "국민발전당",
        "People's Development Party",
        "国民发展党",
        "国民発展党"
    ),
    SAE_ROUN_GUKMIN_DANG(
        "새로운국민당",
        "New National Party",
        "新国民党",
        "新国民党"
    ),
    MINJU_GAEHYUK_DANG(
        "민주개혁당",
        "Democratic Reform Party",
        "民主改革党",
        "民主改革党"
    ),
    GUKMIN_DONGMAENG_DANG(
        "국민동맹당",
        "People's Alliance Party",
        "国民联盟党",
        "国民連盟党"
    ),
    MINJU_JEONGCHAEK_DANG(
        "민주정책당",
        "Democratic Policy Party",
        "民主政策党",
        "民主政策党"
    ),
    KOREAN_REPUBLICAN(
        "공화당",
        "Republican Party", "共和党",
        "共和党"
    ),
    FINANCIAL_REFORM(
        "금융개혁당",
        "Financial Reform Party",
        "金融改革党",
        "金融改革党"
    ),
    CHRISTIAN_PARTY(
        "기독당",
        "Christian Party",
        "基督党",
        "キリスト党"
    ),
    CLIMATE_LIVELIHOOD(
        "기후민생당",
        "Climate and Livelihood Party",
        "气候民生党",
        "気候民生党"
    ),
    TOMORROW_FUTURE(
        "내일로미래로",
        "Tomorrow for the Future",
        "走向未来",
        "明日への未来"
    ),
    LABOR_PARTY(
        "노동당",
        "Labor Party",
        "劳动党",
        "労働党"
    ),
    SENIOR_WELFARE(
        "노인복지당",
        "Senior Welfare Party",
        "老人福利党",
        "高齢者福祉党"
    ),
    GREEN_JUSTICE(
        "녹색정의당",
        "Green Justice Party",
        "绿色正义党",
        "グリーンジャスティス党"
    ),
    FUTURE_PARTY(
        "미래당",
        "Future Party",
        "未来党",
        "未来党"
    ),
    PINE_TREE(
        "소나무당",
        "Pine Tree Party",
        "松树党",
        "松党"
    ),
    NEW_KOREAN_PENINSULA(
        "신한반도당",
        "New Korean Peninsula Party",
        "新韩半岛党",
        "新韓半島党"
    ),
    WOMEN_PARTY(
        "여성의당",
        "Women's Party",
        "女性党",
        "女性党"
    ),
    OUR_REPUBLICAN(
        "우리공화당",
        "Our Republican Party",
        "我们共和党",
        "我ら共和党"
    ),
    LIBERAL_UNIFICATION(
        "자유통일당",
        "Liberal Unification Party",
        "自由统一党",
        "リベラル統一党"
    ),
    UNIFICATION_KOREA(
        "통일한국당",
        "Unification Korea Party",
        "统一韩国党",
        "統一韓国党"
    ),
    KOREAN_FARMERS_FISHERMEN(
        "한국농어민당",
        "Korean Farmers and Fishermen Party",
        "韩国农渔民党",
        "韓国農漁民党"
    );

    companion object {
        val lookupByKoValue: Map<String, Party> = entries.associateBy { it.koValue }
    }
}