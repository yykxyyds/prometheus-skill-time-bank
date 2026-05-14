const pptxgen = require("pptxgenjs");

const pres = new pptxgen();
pres.layout = "LAYOUT_16x9"; // 10 x 5.625 inches
pres.author = "第10组";
pres.title = "Prometheus——技能时间银行";

// ============ COLOR PALETTE ============
const C = {
  bg: "FEF5EB",
  orange: "F29D38",
  teal: "57A4B9",
  coral: "F05F42",
  darkTeal: "285460",
  dark: "2C3E50",
  gray: "888888",
  lightGray: "BBBBBB",
  white: "FFFFFF",
  cardBg: "FCFAF8",
  orangeLight: "FDE8D0",
  tealLight: "D4EDF0",
  coralLight: "FDE0DA",
};

// ============ HELPER FUNCTIONS ============
function makeShadow(opacity = 0.08) {
  return { type: "outer", blur: 6, offset: 2, angle: 135, color: "000000", opacity };
}

function addDecorCircle(slide, x, y, size, color, opacity = 1) {
  const opt = { x, y, w: size, h: size, fill: { color }, };
  if (opacity < 1) opt.fill.transparency = Math.round((1 - opacity) * 100);
  slide.addShape(pres.shapes.OVAL, opt);
}

function addDecorRoundedRect(slide, x, y, w, h, color, opacity = 1) {
  const opt = { x, y, w, h, fill: { color }, rectRadius: 0.05 };
  if (opacity < 1) opt.fill.transparency = Math.round((1 - opacity) * 100);
  slide.addShape(pres.shapes.ROUNDED_RECTANGLE, opt);
}

function addCard(slide, x, y, w, h) {
  slide.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x, y, w, h,
    fill: { color: C.white },
    shadow: makeShadow(0.10),
    rectRadius: 0.12,
  });
}

function addTitle(slide, text, y = 0.4) {
  slide.addText(text, {
    x: 0.8, y, w: 8.4, h: 0.7,
    fontSize: 30, fontFace: "Microsoft YaHei",
    color: C.dark, bold: true, align: "left",
  });
}

function addBottomLine(slide, text) {
  slide.addText(text, {
    x: 0.8, y: 4.85, w: 8.4, h: 0.4,
    fontSize: 12, fontFace: "Microsoft YaHei",
    color: C.lightGray, align: "center",
  });
}

// ============ SLIDE 1: 封面 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };

  // Decorative shapes - right side organic blobs
  addDecorCircle(slide, 7.2, -0.6, 3.5, C.orange, 0.7);
  addDecorCircle(slide, 8.5, 1.8, 2.8, C.teal, 0.5);
  addDecorCircle(slide, 7.8, 3.5, 2.0, C.teal, 0.3);
  addDecorCircle(slide, 6.5, -0.3, 1.2, C.orange, 0.4);
  addDecorRoundedRect(slide, 9.0, 3.0, 0.3, 0.8, C.coral, 0.6);

  // Main title
  slide.addText("Prometheus", {
    x: 0.8, y: 1.2, w: 7.0, h: 1.0,
    fontSize: 54, fontFace: "Microsoft YaHei",
    color: C.dark, bold: true, align: "left",
  });
  slide.addText("技能时间银行", {
    x: 0.8, y: 2.05, w: 7.0, h: 0.8,
    fontSize: 44, fontFace: "Microsoft YaHei",
    color: C.orange, bold: true, align: "left",
  });
  slide.addText("以时间币为核心的技能互助平台", {
    x: 0.8, y: 2.9, w: 7.0, h: 0.5,
    fontSize: 18, fontFace: "Microsoft YaHei",
    color: C.gray, align: "left",
  });

  // Left accent bar
  slide.addShape(pres.shapes.RECTANGLE, {
    x: 0.5, y: 1.2, w: 0.06, h: 1.65,
    fill: { color: C.orange },
  });

  // Bottom info
  slide.addText("第10组  |  指导教师：刘德才", {
    x: 0.8, y: 4.4, w: 5.0, h: 0.4,
    fontSize: 14, fontFace: "Microsoft YaHei",
    color: C.gray, align: "left",
  });
  slide.addText("海南大学课程设计答辩  ·  2026", {
    x: 0.8, y: 4.8, w: 5.0, h: 0.35,
    fontSize: 11, fontFace: "Microsoft YaHei",
    color: C.lightGray, align: "left",
  });
}

// ============ SLIDE 2: 目录 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };

  // Decorative
  addDecorCircle(slide, -0.5, -0.5, 1.8, C.orange, 0.3);
  addDecorCircle(slide, 9.0, 3.8, 2.0, C.teal, 0.2);

  slide.addText("目录", {
    x: 0, y: 0.4, w: 10, h: 0.8,
    fontSize: 40, fontFace: "Microsoft YaHei",
    color: C.dark, bold: true, align: "center",
  });

  const cards = [
    { num: "01", color: C.orange, title: "项目概述与团队分工", sub: "项目背景、核心功能、团队成员" },
    { num: "02", color: C.teal, title: "技术架构与核心设计", sub: "技术栈、架构设计、数据库、关键亮点" },
    { num: "03", color: C.coral, title: "项目总结与展望", sub: "项目成果、AI辅助开发、未来规划" },
  ];

  cards.forEach((c, i) => {
    const yBase = 1.6 + i * 1.3;
    addCard(slide, 1.5, yBase, 7.0, 1.05);

    // Number circle
    slide.addShape(pres.shapes.OVAL, {
      x: 1.8, y: yBase + 0.15, w: 0.75, h: 0.75,
      fill: { color: c.color },
    });
    slide.addText(c.num, {
      x: 1.8, y: yBase + 0.15, w: 0.75, h: 0.75,
      fontSize: 28, fontFace: "Microsoft YaHei",
      color: C.white, bold: true, align: "center", valign: "middle",
    });

    slide.addText(c.title, {
      x: 2.8, y: yBase + 0.12, w: 5.2, h: 0.45,
      fontSize: 19, fontFace: "Microsoft YaHei",
      color: C.dark, bold: true, align: "left",
    });
    slide.addText(c.sub, {
      x: 2.8, y: yBase + 0.55, w: 5.2, h: 0.35,
      fontSize: 12, fontFace: "Microsoft YaHei",
      color: C.gray, align: "left",
    });
  });
}

// ============ SLIDE 3: 章节页 01 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };

  addDecorCircle(slide, -1.0, 3.0, 3.5, C.orange, 0.15);
  addDecorCircle(slide, 1.5, -0.5, 1.6, C.orange, 0.25);
  addDecorRoundedRect(slide, 0.5, 1.0, 0.08, 1.6, C.teal, 0.8);

  slide.addText("01", {
    x: 1.0, y: 1.2, w: 2.5, h: 1.4,
    fontSize: 80, fontFace: "Microsoft YaHei",
    color: C.orange, bold: true, align: "left",
  });
  slide.addText("项目概述与团队分工", {
    x: 3.5, y: 1.5, w: 5.5, h: 0.8,
    fontSize: 32, fontFace: "Microsoft YaHei",
    color: C.darkTeal, bold: true, align: "left",
  });
  slide.addText("PROJECT OVERVIEW & TEAM", {
    x: 3.5, y: 2.3, w: 5.5, h: 0.4,
    fontSize: 14, fontFace: "Arial",
    color: C.gray, align: "left",
  });
}

// ============ SLIDE 4: 项目背景与意义 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "项目背景与意义");

  // Pain points column
  slide.addText("痛点", {
    x: 0.8, y: 1.3, w: 4.0, h: 0.5,
    fontSize: 20, fontFace: "Microsoft YaHei",
    color: C.coral, bold: true, align: "left",
  });
  addCard(slide, 0.8, 1.8, 3.8, 2.6);

  const pains = [
    "技能闲置，无法有效变现",
    "传统技能交易，门槛高、流程繁琐",
    "陌生人之间，缺乏信任机制",
  ];
  pains.forEach((p, i) => {
    slide.addText("✕", {
      x: 1.1, y: 2.0 + i * 0.75, w: 0.4, h: 0.4,
      fontSize: 16, color: C.coral, align: "center",
    });
    slide.addText(p, {
      x: 1.6, y: 2.0 + i * 0.75, w: 2.8, h: 0.4,
      fontSize: 14, fontFace: "Microsoft YaHei",
      color: C.dark, align: "left",
    });
  });

  // Solution column
  slide.addText("我们的方案", {
    x: 5.4, y: 1.3, w: 4.0, h: 0.5,
    fontSize: 20, fontFace: "Microsoft YaHei",
    color: C.teal, bold: true, align: "left",
  });
  addCard(slide, 5.4, 1.8, 3.8, 2.6);

  const sols = [
    "时间币体系，量化技能价值",
    "一站式技能共享平台，降低交易成本",
    "双盲评价+信誉雷达，建立信任体系",
  ];
  sols.forEach((s, i) => {
    slide.addText("✓", {
      x: 5.7, y: 2.0 + i * 0.75, w: 0.4, h: 0.4,
      fontSize: 16, color: C.teal, align: "center",
    });
    slide.addText(s, {
      x: 6.2, y: 2.0 + i * 0.75, w: 2.8, h: 0.4,
      fontSize: 14, fontFace: "Microsoft YaHei",
      color: C.dark, align: "left",
    });
  });

  // Goal
  slide.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.8, y: 4.65, w: 8.4, h: 0.45,
    fill: { color: C.orangeLight },
    rectRadius: 0.1,
  });
  slide.addText("目标：构建一个拒绝金钱交易、回归价值交换的技能互助社区", {
    x: 0.8, y: 4.65, w: 8.4, h: 0.45,
    fontSize: 13, fontFace: "Microsoft YaHei",
    color: C.orange, align: "center", valign: "middle",
    bold: true,
  });
}

// ============ SLIDE 5: 团队分工 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "团队分工");

  const members = [
    { name: "郑朴", role: "项目负责人", desc: "需求分析、进度管理\n后端架构设计", color: C.orange },
    { name: "吴仁杰", role: "后端+前端开发", desc: "Spring Boot多模块\nVue 3前端、数据库设计", color: C.teal },
    { name: "黄煜乾", role: "文档+PPT", desc: "SRS需求规格、详细设计\n答辩PPT、部署说明", color: C.coral },
    { name: "胡子风", role: "测试", desc: "功能测试、接口测试\nBug跟踪与回归验证", color: C.teal },
  ];

  members.forEach((m, i) => {
    const col = i % 2;
    const row = Math.floor(i / 2);
    const x = 0.8 + col * 4.3;
    const y = 1.3 + row * 2.0;

    addCard(slide, x, y, 3.9, 1.75);

    // Color accent top
    slide.addShape(pres.shapes.RECTANGLE, {
      x: x + 0.2, y: y + 0.15, w: 0.06, h: 0.65,
      fill: { color: m.color },
    });

    slide.addText(m.name, {
      x: x + 0.45, y: y + 0.15, w: 2.0, h: 0.4,
      fontSize: 18, fontFace: "Microsoft YaHei",
      color: C.dark, bold: true, align: "left",
    });
    slide.addText(m.role, {
      x: x + 0.45, y: y + 0.55, w: 2.5, h: 0.3,
      fontSize: 13, fontFace: "Microsoft YaHei",
      color: m.color, bold: true, align: "left",
    });
    slide.addText(m.desc, {
      x: x + 0.25, y: y + 0.95, w: 3.4, h: 0.65,
      fontSize: 12, fontFace: "Microsoft YaHei",
      color: C.gray, align: "left", lineSpacingMultiple: 1.4,
    });
  });

  addBottomLine(slide, "明确分工  /  高效协作  /  共同推进");
}

// ============ SLIDE 6: 核心功能 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "核心功能");

  const features = [
    { icon: "▣", title: "技能广场", desc: "分类浏览、关键词搜索\n技能发布与下架管理", color: C.orange },
    { icon: "◎", title: "需求悬赏", desc: "发布需求、设置悬赏金\n申请接单、验收完成", color: C.teal },
    { icon: "◇", title: "时间银行", desc: "钱包余额、时间流水\n时间币冻结与转账", color: C.coral },
    { icon: "⇄", title: "订单交易", desc: "下单→确认→完成全流程\n5态状态机自动流转", color: C.teal },
    { icon: "⛬", title: "双盲评价", desc: "交易后双方互评\n7天自动解盲+信誉雷达图", color: C.orange },
    { icon: "⚙", title: "管理后台", desc: "用户管理、技能审核\n申诉处理、公告发布", color: C.coral },
  ];

  features.forEach((f, i) => {
    const col = i % 3;
    const row = Math.floor(i / 3);
    const x = 0.7 + col * 3.0;
    const y = 1.3 + row * 2.1;

    addCard(slide, x, y, 2.65, 1.85);

    // Icon circle
    slide.addShape(pres.shapes.OVAL, {
      x: x + 0.8, y: y + 0.2, w: 0.7, h: 0.7,
      fill: { color: f.color, transparency: 15 },
    });
    slide.addText(f.icon, {
      x: x + 0.8, y: y + 0.2, w: 0.7, h: 0.7,
      fontSize: 22, color: f.color, align: "center", valign: "middle",
    });

    slide.addText(f.title, {
      x: x + 0.15, y: y + 1.05, w: 2.35, h: 0.35,
      fontSize: 15, fontFace: "Microsoft YaHei",
      color: C.dark, bold: true, align: "center",
    });
    slide.addText(f.desc, {
      x: x + 0.15, y: y + 1.4, w: 2.35, h: 0.4,
      fontSize: 10.5, fontFace: "Microsoft YaHei",
      color: C.gray, align: "center", lineSpacingMultiple: 1.3,
    });
  });
}

// ============ SLIDE 7: 功能展示 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "功能展示");

  // Left: user side
  slide.addText("用户端", {
    x: 0.8, y: 1.2, w: 3.5, h: 0.4,
    fontSize: 16, fontFace: "Microsoft YaHei",
    color: C.orange, bold: true, align: "left",
  });

  const userPages = ["技能广场首页", "技能详情", "私信聊天", "时间钱包", "订单详情", "个人中心"];
  userPages.forEach((p, i) => {
    const col = i % 3;
    const row = Math.floor(i / 3);
    const x = 0.8 + col * 1.5;
    const y = 1.7 + row * 1.65;

    slide.addShape(pres.shapes.RECTANGLE, {
      x, y, w: 1.3, h: 1.3,
      fill: { color: C.cardBg },
      line: { color: "F0E8E0", width: 0.75 },
    });
    slide.addText("[ 截图 ]", {
      x, y, w: 1.3, h: 1.3,
      fontSize: 9, fontFace: "Microsoft YaHei",
      color: C.lightGray, align: "center", valign: "middle",
    });
    slide.addText(p, {
      x, y: y + 1.33, w: 1.3, h: 0.25,
      fontSize: 9, fontFace: "Microsoft YaHei",
      color: C.gray, align: "center",
    });
  });

  // Divider
  slide.addShape(pres.shapes.RECTANGLE, {
    x: 5.3, y: 1.3, w: 0.015, h: 3.8,
    fill: { color: "F0E8E0" },
  });

  // Right: admin side
  slide.addText("管理后台", {
    x: 5.7, y: 1.2, w: 3.5, h: 0.4,
    fontSize: 16, fontFace: "Microsoft YaHei",
    color: C.teal, bold: true, align: "left",
  });

  const adminPages = [
    { name: "用户管理", desc: "启用/禁用用户" },
    { name: "技能审核", desc: "通过/拒绝技能" },
    { name: "申诉处理", desc: "处理交易申诉" },
    { name: "公告管理", desc: "发布/编辑公告" },
  ];
  adminPages.forEach((p, i) => {
    const col = i % 2;
    const row = Math.floor(i / 2);
    const x = 5.7 + col * 2.0;
    const y = 1.7 + row * 1.7;

    slide.addShape(pres.shapes.RECTANGLE, {
      x, y, w: 1.75, h: 1.35,
      fill: { color: C.cardBg },
      line: { color: "F0E8E0", width: 0.75 },
    });
    slide.addText("[ 截图 ]", {
      x, y, w: 1.75, h: 1.0,
      fontSize: 9, fontFace: "Microsoft YaHei",
      color: C.lightGray, align: "center", valign: "middle",
    });
    slide.addText(p.name, {
      x, y: y + 1.0, w: 1.75, h: 0.35,
      fontSize: 11, fontFace: "Microsoft YaHei",
      color: C.dark, bold: true, align: "center",
    });
  });
}

// ============ SLIDE 8: 章节页 02 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };

  addDecorCircle(slide, -1.0, 3.0, 3.5, C.teal, 0.15);
  addDecorCircle(slide, 2.0, -0.5, 1.8, C.teal, 0.2);
  addDecorRoundedRect(slide, 0.5, 1.0, 0.08, 1.6, C.teal, 0.8);

  slide.addText("02", {
    x: 1.0, y: 1.2, w: 2.5, h: 1.4,
    fontSize: 80, fontFace: "Microsoft YaHei",
    color: C.teal, bold: true, align: "left",
  });
  slide.addText("技术架构与核心设计", {
    x: 3.5, y: 1.5, w: 5.5, h: 0.8,
    fontSize: 32, fontFace: "Microsoft YaHei",
    color: C.darkTeal, bold: true, align: "left",
  });
  slide.addText("TECHNICAL ARCHITECTURE & DESIGN", {
    x: 3.5, y: 2.3, w: 5.5, h: 0.4,
    fontSize: 14, fontFace: "Arial",
    color: C.gray, align: "left",
  });
}

// ============ SLIDE 9: 技术架构总览 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "技术架构总览");

  // Architecture boxes with arrows
  const drawBox = (x, y, w, h, label, color, sublabel = "") => {
    slide.addShape(pres.shapes.ROUNDED_RECTANGLE, {
      x, y, w, h,
      fill: { color, transparency: color === C.white ? 0 : 12 },
      line: { color, width: 1.5 },
      rectRadius: 0.08,
    });
    slide.addText(label, {
      x, y, w, h,
      fontSize: 11, fontFace: "Microsoft YaHei",
      color: color === C.white ? C.dark : C.white,
      bold: true, align: "center", valign: "middle",
    });
    if (sublabel) {
      slide.addText(sublabel, {
        x, y: y + h - 0.15, w, h: 0.15,
        fontSize: 8, fontFace: "Arial",
        color: color === C.white ? C.gray : "E0E0E0",
        align: "center",
      });
    }
  };

  const drawArrow = (x, y, w, h) => {
    slide.addText("▼", {
      x, y, w, h,
      fontSize: 14, color: C.gray, align: "center", valign: "middle",
    });
  };

  // Frontend layer
  drawBox(1.0, 1.2, 3.5, 0.7, "Vue 3 + Vite 8 用户前端", C.orange, "localhost:5173");
  drawBox(5.5, 1.2, 3.5, 0.7, "Vue 3 管理后台", C.orange, "localhost:5174");

  drawArrow(0, 1.95, 10, 0.3);

  // Gateway layer
  drawBox(2.5, 2.25, 5.0, 0.7, "skill-gateway  统一入口 :8080", C.teal);
  drawArrow(0, 3.0, 10, 0.3);

  // Service modules
  const modules = ["common", "user", "skill", "order", "wallet", "admin"];
  modules.forEach((m, i) => {
    drawBox(1.0 + i * 1.4, 3.3, 1.2, 0.55, m, "FFFFFF", " ");
  });
  // Override module text color
  modules.forEach((m, i) => {
    // redraw with proper styling
    slide.addShape(pres.shapes.ROUNDED_RECTANGLE, {
      x: 1.0 + i * 1.4, y: 3.3, w: 1.2, h: 0.55,
      fill: { color: C.orange, transparency: 75 },
      line: { color: C.orange, width: 1 },
      rectRadius: 0.06,
    });
    slide.addText(m, {
      x: 1.0 + i * 1.4, y: 3.3, w: 1.2, h: 0.55,
      fontSize: 10, fontFace: "Arial",
      color: C.dark, bold: true, align: "center", valign: "middle",
    });
  });

  drawArrow(0, 3.9, 10, 0.3);

  // Database layer
  drawBox(2.5, 4.2, 5.0, 0.55, "MySQL 8.0  (14张表)", C.darkTeal);

  // Tech stack labels
  const techs = ["Spring Boot 3.2", "MyBatis-Plus 3.5.5", "JWT认证", "Element Plus", "Pinia", "Axios"];
  techs.forEach((t, i) => {
    const tx = 0.8 + i * 1.55;
    slide.addShape(pres.shapes.ROUNDED_RECTANGLE, {
      x: tx, y: 5.05, w: 1.35, h: 0.32,
      fill: { color: C.cardBg },
      line: { color: "F0E8E0", width: 0.5 },
      rectRadius: 0.08,
    });
    slide.addText(t, {
      x: tx, y: 5.05, w: 1.35, h: 0.32,
      fontSize: 9, fontFace: "Arial",
      color: C.gray, align: "center", valign: "middle",
    });
  });
}

// ============ SLIDE 10: 关键技术亮点 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "关键技术亮点");

  const highlights = [
    {
      title: "JWT认证体系",
      color: C.orange,
      desc: "自定义@RequireAuth注解 + 全局拦截器\n登录态管理，角色权限控制\nToken自动续期与失效处理",
    },
    {
      title: "订单状态机",
      color: C.teal,
      desc: "待确认→进行中→待确认完成→已完成/已取消\n时间币自动冻结/解冻/转账\n双方确认机制保障交易安全",
    },
    {
      title: "雪花ID处理",
      color: C.coral,
      desc: "Snowflake算法生成分布式唯一ID\nJackson Long→String序列化\n彻底解决前端JavaScript精度丢失问题",
    },
    {
      title: "双盲评价机制",
      color: C.teal,
      desc: "双方互评后立即可见评价内容\n7天超时自动解盲\n四维信誉雷达图(按时/沟通/专业/态度)",
    },
  ];

  highlights.forEach((h, i) => {
    const col = i % 2;
    const row = Math.floor(i / 2);
    const x = 0.8 + col * 4.3;
    const y = 1.3 + row * 2.0;

    addCard(slide, x, y, 3.9, 1.75);

    // Colored left accent
    slide.addShape(pres.shapes.RECTANGLE, {
      x, y, w: 0.07, h: 1.75,
      fill: { color: h.color },
    });

    slide.addText(h.title, {
      x: x + 0.25, y: y + 0.15, w: 3.4, h: 0.35,
      fontSize: 16, fontFace: "Microsoft YaHei",
      color: C.dark, bold: true, align: "left",
    });
    slide.addText(h.desc, {
      x: x + 0.25, y: y + 0.6, w: 3.4, h: 1.0,
      fontSize: 11.5, fontFace: "Microsoft YaHei",
      color: C.gray, align: "left", lineSpacingMultiple: 1.5,
    });
  });
}

// ============ SLIDE 11: 数据库设计 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "数据库设计");

  // Left: table list
  addCard(slide, 0.8, 1.3, 5.0, 3.5);

  const tables = [
    "user (用户)", "skill (技能)", "skill_category (分类)",
    "bounty (悬赏)", "bounty_application (申请)", "skill_order (订单)",
    "time_transaction (流水)", "review (评价)", "appeal (申诉)",
    "announcement (公告)", "private_message (私信)", "chat_message (聊天)",
    "user_follow (关注)", "notification (通知)",
  ];

  tables.forEach((t, i) => {
    const col = i % 2;
    const row = Math.floor(i / 2);
    slide.addText("▸  " + t, {
      x: 1.1 + col * 2.4, y: 1.45 + row * 0.44, w: 2.3, h: 0.4,
      fontSize: 11, fontFace: "Microsoft YaHei",
      color: C.dark, align: "left",
    });
  });

  // Right: technical points
  slide.addText("技术要点", {
    x: 6.2, y: 1.3, w: 3.0, h: 0.4,
    fontSize: 16, fontFace: "Microsoft YaHei",
    color: C.teal, bold: true, align: "left",
  });

  const points = [
    { label: "雪花ID主键", desc: "分布式唯一，趋势递增" },
    { label: "BCrypt加密", desc: "密码安全存储，防彩虹表" },
    { label: "范式设计 (3NF)", desc: "消除数据冗余，保证一致性" },
    { label: "BaseEntity", desc: "公共字段：id/createTime/updateTime" },
  ];

  points.forEach((p, i) => {
    const y = 1.85 + i * 0.75;
    addCard(slide, 6.2, y, 3.2, 0.6);

    slide.addShape(pres.shapes.RECTANGLE, {
      x: 6.2, y, w: 0.06, h: 0.6,
      fill: { color: C.teal },
    });
    slide.addText(p.label, {
      x: 6.45, y: y + 0.05, w: 2.7, h: 0.28,
      fontSize: 12, fontFace: "Microsoft YaHei",
      color: C.dark, bold: true, align: "left",
    });
    slide.addText(p.desc, {
      x: 6.45, y: y + 0.3, w: 2.7, h: 0.25,
      fontSize: 10, fontFace: "Microsoft YaHei",
      color: C.gray, align: "left",
    });
  });

  addBottomLine(slide, "共14张表，完整覆盖业务需求");
}

// ============ SLIDE 12: 章节页 03 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };

  addDecorCircle(slide, -1.0, 3.0, 3.5, C.coral, 0.15);
  addDecorCircle(slide, 2.0, -0.3, 1.8, C.coral, 0.2);
  addDecorRoundedRect(slide, 0.5, 1.0, 0.08, 1.6, C.coral, 0.8);

  slide.addText("03", {
    x: 1.0, y: 1.2, w: 2.5, h: 1.4,
    fontSize: 80, fontFace: "Microsoft YaHei",
    color: C.coral, bold: true, align: "left",
  });
  slide.addText("项目总结与展望", {
    x: 3.5, y: 1.5, w: 5.5, h: 0.8,
    fontSize: 32, fontFace: "Microsoft YaHei",
    color: C.darkTeal, bold: true, align: "left",
  });
  slide.addText("SUMMARY & OUTLOOK", {
    x: 3.5, y: 2.3, w: 5.5, h: 0.4,
    fontSize: 14, fontFace: "Arial",
    color: C.gray, align: "left",
  });
}

// ============ SLIDE 13: 项目成果与AI辅助开发 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "项目成果与AI辅助开发");

  // Left: achievements
  addCard(slide, 0.8, 1.3, 3.9, 3.35);
  slide.addText("项目成果", {
    x: 1.1, y: 1.45, w: 3.4, h: 0.4,
    fontSize: 18, fontFace: "Microsoft YaHei",
    color: C.orange, bold: true, align: "left",
  });

  const achievements = [
    "7个Maven模块全部编译通过",
    "14张数据库表完整设计",
    "13+前端页面完整实现",
    "30+ API接口全覆盖",
    "8项课程交付物全部完成",
  ];
  achievements.forEach((a, i) => {
    slide.addText("✓", {
      x: 1.1, y: 2.05 + i * 0.5, w: 0.3, h: 0.35,
      fontSize: 14, color: C.orange, align: "center",
    });
    slide.addText(a, {
      x: 1.5, y: 2.05 + i * 0.5, w: 3.0, h: 0.35,
      fontSize: 13, fontFace: "Microsoft YaHei",
      color: C.dark, align: "left",
    });
  });

  // Right: AI assisted
  addCard(slide, 5.3, 1.3, 3.9, 3.35);
  slide.addText("AI辅助开发", {
    x: 5.6, y: 1.45, w: 3.4, h: 0.4,
    fontSize: 18, fontFace: "Microsoft YaHei",
    color: C.teal, bold: true, align: "left",
  });

  const aiItems = [
    { label: "全流程AI辅助", desc: "使用Claude Code从零搭建项目" },
    { label: "多Agent协作", desc: "开发/测试/文档Agent并行工作" },
    { label: "排坑驱动迭代", desc: "30+排坑记录，持续优化代码质量" },
    { label: "融入课程要求", desc: "AI辅助开发满足课程设计规范" },
  ];
  aiItems.forEach((a, i) => {
    const sy = 2.0 + i * 0.62;
    slide.addShape(pres.shapes.OVAL, {
      x: 5.6, y: sy + 0.06, w: 0.18, h: 0.18,
      fill: { color: C.teal },
    });
    slide.addText(a.label, {
      x: 5.95, y: sy, w: 3.0, h: 0.28,
      fontSize: 12, fontFace: "Microsoft YaHei",
      color: C.dark, bold: true, align: "left",
    });
    slide.addText(a.desc, {
      x: 5.95, y: sy + 0.25, w: 3.0, h: 0.25,
      fontSize: 10.5, fontFace: "Microsoft YaHei",
      color: C.gray, align: "left",
    });
  });

  // Bottom
  slide.addShape(pres.shapes.ROUNDED_RECTANGLE, {
    x: 0.8, y: 4.9, w: 8.4, h: 0.45,
    fill: { color: C.tealLight },
    rectRadius: 0.1,
  });
  slide.addText("课程要求全部满足：分层架构  |  前后端分离  |  14张表  |  JWT认证  |  Git版本管理  |  AI融入", {
    x: 0.8, y: 4.9, w: 8.4, h: 0.45,
    fontSize: 11, fontFace: "Microsoft YaHei",
    color: C.teal, align: "center", valign: "middle", bold: true,
  });
}

// ============ SLIDE 14: 致谢 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };

  // Decorative shapes
  addDecorCircle(slide, -0.8, -0.5, 3.0, C.teal, 0.15);
  addDecorCircle(slide, -0.3, 2.5, 2.0, C.orange, 0.12);
  addDecorCircle(slide, 7.5, -0.5, 3.0, C.orange, 0.12);
  addDecorCircle(slide, 8.5, 2.0, 2.5, C.teal, 0.10);

  slide.addText("THANK YOU", {
    x: 0, y: 1.2, w: 10, h: 1.2,
    fontSize: 56, fontFace: "Microsoft YaHei",
    color: C.dark, bold: true, align: "center",
  });
  slide.addText("感谢聆听", {
    x: 0, y: 2.5, w: 10, h: 0.7,
    fontSize: 30, fontFace: "Microsoft YaHei",
    color: C.coral, bold: true, align: "center",
  });

  // Decorative line
  slide.addShape(pres.shapes.RECTANGLE, {
    x: 3.5, y: 3.5, w: 3.0, h: 0.03,
    fill: { color: C.lightGray },
  });

  slide.addText("指导教师：刘德才", {
    x: 0, y: 3.85, w: 10, h: 0.45,
    fontSize: 16, fontFace: "Microsoft YaHei",
    color: C.gray, align: "center",
  });
  slide.addText("第10组全体成员：郑朴  ·  吴仁杰  ·  黄煜乾  ·  胡子风", {
    x: 0, y: 4.3, w: 10, h: 0.4,
    fontSize: 14, fontFace: "Microsoft YaHei",
    color: C.lightGray, align: "center",
  });
  slide.addText("Prometheus——技能时间银行  ·  2026", {
    x: 0, y: 4.8, w: 10, h: 0.35,
    fontSize: 11, fontFace: "Microsoft YaHei",
    color: C.lightGray, align: "center",
  });
}

// ============ OUTPUT ============
pres.writeFile({ fileName: "D:/Agent工作区/Claude Code工作区/综合实训项目/document/答辩PPT-第10组.pptx" })
  .then(() => console.log("PPT generated successfully!"))
  .catch(err => console.error("Error:", err));
