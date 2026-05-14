const pptxgen = require("pptxgenjs");
const fs = require("fs");
const path = require("path");

const pres = new pptxgen();
pres.layout = "LAYOUT_16x9";
pres.author = "第10组";
pres.title = "Prometheus——技能时间银行";

const IMG = "D:/Agent工作区/Claude Code工作区/综合实训项目/screenshots";

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

// ============ HELPERS ============
function makeShadow(opacity = 0.08) {
  return { type: "outer", blur: 6, offset: 2, angle: 135, color: "000000", opacity };
}
function makeCardShadow() {
  return { type: "outer", blur: 6, offset: 2, angle: 135, color: "000000", opacity: 0.10 };
}

function addDecorCircle(slide, x, y, size, color, opacity = 1) {
  const opt = { x, y, w: size, h: size, fill: { color } };
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
    x, y, w, h, fill: { color: C.white }, shadow: makeCardShadow(), rectRadius: 0.12,
  });
}

function addTitle(slide, text, y = 0.4) {
  slide.addText(text, {
    x: 0.8, y, w: 8.4, h: 0.7,
    fontSize: 30, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "left",
  });
}

function addBottomLine(slide, text) {
  slide.addText(text, {
    x: 0.8, y: 4.85, w: 8.4, h: 0.4,
    fontSize: 12, fontFace: "Microsoft YaHei", color: C.lightGray, align: "center",
  });
}

function addImg(slide, filename, x, y, w, h) {
  const p = path.join(IMG, filename);
  if (fs.existsSync(p)) {
    slide.addImage({ path: p, x, y, w, h, sizing: { type: "cover", w, h } });
  }
}

// ============ SLIDE 1: 封面 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addDecorCircle(slide, 7.2, -0.6, 3.5, C.orange, 0.7);
  addDecorCircle(slide, 8.5, 1.8, 2.8, C.teal, 0.5);
  addDecorCircle(slide, 7.8, 3.5, 2.0, C.teal, 0.3);
  addDecorCircle(slide, 6.5, -0.3, 1.2, C.orange, 0.4);
  addDecorRoundedRect(slide, 9.0, 3.0, 0.3, 0.8, C.coral, 0.6);
  slide.addShape(pres.shapes.RECTANGLE, { x: 0.5, y: 1.2, w: 0.06, h: 1.65, fill: { color: C.orange } });
  slide.addText("Prometheus", { x: 0.8, y: 1.2, w: 7.0, h: 1.0, fontSize: 54, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "left" });
  slide.addText("技能时间银行", { x: 0.8, y: 2.05, w: 7.0, h: 0.8, fontSize: 44, fontFace: "Microsoft YaHei", color: C.orange, bold: true, align: "left" });
  slide.addText("以时间币为核心的技能互助平台", { x: 0.8, y: 2.9, w: 7.0, h: 0.5, fontSize: 18, fontFace: "Microsoft YaHei", color: C.gray, align: "left" });
  slide.addText("第10组  |  指导教师：刘德才", { x: 0.8, y: 4.4, w: 5.0, h: 0.4, fontSize: 14, fontFace: "Microsoft YaHei", color: C.gray, align: "left" });
  slide.addText("海南大学课程设计答辩  ·  2026", { x: 0.8, y: 4.8, w: 5.0, h: 0.35, fontSize: 11, fontFace: "Microsoft YaHei", color: C.lightGray, align: "left" });
}

// ============ SLIDE 2: 目录 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addDecorCircle(slide, -0.5, -0.5, 1.8, C.orange, 0.3);
  addDecorCircle(slide, 9.0, 3.8, 2.0, C.teal, 0.2);
  slide.addText("目录", { x: 0, y: 0.4, w: 10, h: 0.8, fontSize: 40, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "center" });
  const cards = [
    { num: "01", color: C.orange, title: "项目概述与团队分工", sub: "项目背景、核心功能、团队成员" },
    { num: "02", color: C.teal, title: "技术架构与核心设计", sub: "技术栈、架构设计、数据库、关键亮点" },
    { num: "03", color: C.coral, title: "项目总结与展望", sub: "项目成果、AI辅助开发、未来规划" },
  ];
  cards.forEach((c, i) => {
    const yBase = 1.6 + i * 1.3;
    addCard(slide, 1.5, yBase, 7.0, 1.05);
    slide.addShape(pres.shapes.OVAL, { x: 1.8, y: yBase + 0.15, w: 0.75, h: 0.75, fill: { color: c.color } });
    slide.addText(c.num, { x: 1.8, y: yBase + 0.15, w: 0.75, h: 0.75, fontSize: 28, fontFace: "Microsoft YaHei", color: C.white, bold: true, align: "center", valign: "middle" });
    slide.addText(c.title, { x: 2.8, y: yBase + 0.12, w: 5.2, h: 0.45, fontSize: 19, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "left" });
    slide.addText(c.sub, { x: 2.8, y: yBase + 0.55, w: 5.2, h: 0.35, fontSize: 12, fontFace: "Microsoft YaHei", color: C.gray, align: "left" });
  });
}

// ============ SLIDE 3: 章节页 01 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addDecorCircle(slide, -1.0, 3.0, 3.5, C.orange, 0.15);
  addDecorCircle(slide, 1.5, -0.5, 1.6, C.orange, 0.25);
  addDecorRoundedRect(slide, 0.5, 1.0, 0.08, 1.6, C.teal, 0.8);
  slide.addText("01", { x: 1.0, y: 1.2, w: 2.5, h: 1.4, fontSize: 80, fontFace: "Microsoft YaHei", color: C.orange, bold: true, align: "left" });
  slide.addText("项目概述与团队分工", { x: 3.5, y: 1.5, w: 5.5, h: 0.8, fontSize: 32, fontFace: "Microsoft YaHei", color: C.darkTeal, bold: true, align: "left" });
  slide.addText("PROJECT OVERVIEW & TEAM", { x: 3.5, y: 2.3, w: 5.5, h: 0.4, fontSize: 14, fontFace: "Arial", color: C.gray, align: "left" });
}

// ============ SLIDE 4: 项目背景与意义 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "项目背景与意义");
  slide.addText(
    "在大学生群体中，技能不对称现象普遍存在——有人擅长设计却闲置能力，有人急需技术指导却无从获取。如何有效连接技能供给与需求，让每个人的才华都能产生价值？",
    { x: 0.8, y: 1.1, w: 8.4, h: 0.65, fontSize: 13, fontFace: "Microsoft YaHei", color: C.gray, align: "left", lineSpacingMultiple: 1.4 }
  );
  slide.addText("痛点分析", { x: 0.8, y: 1.85, w: 4.0, h: 0.45, fontSize: 20, fontFace: "Microsoft YaHei", color: C.coral, bold: true, align: "left" });
  addCard(slide, 0.8, 2.3, 3.8, 2.15);
  const pains = [
    ["技能闲置", "大量技能持有者没有变现渠道，能力被困置"],
    ["交易门槛", "传统技能交易流程复杂，时间成本与金钱成本高"],
    ["信任缺失", "陌生人之间缺乏信用背书，交易风险难以评估"],
  ];
  pains.forEach((p, i) => {
    slide.addText(p[0], { x: 1.1, y: 2.45 + i * 0.68, w: 3.4, h: 0.28, fontSize: 14, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "left" });
    slide.addText("✕ " + p[1], { x: 1.1, y: 2.72 + i * 0.68, w: 3.4, h: 0.3, fontSize: 11, fontFace: "Microsoft YaHei", color: C.gray, align: "left" });
  });
  slide.addText("我们的方案", { x: 5.4, y: 1.85, w: 4.0, h: 0.45, fontSize: 20, fontFace: "Microsoft YaHei", color: C.teal, bold: true, align: "left" });
  addCard(slide, 5.4, 2.3, 3.8, 2.15);
  const sols = [
    ["时间币体系", "以时间币量化技能价值，拒绝金钱交易"],
    ["共享平台", "一站式技能发布与交易，降低中间成本"],
    ["信誉机制", "双盲评价+四维雷达图，建立可信社区"],
  ];
  sols.forEach((s, i) => {
    slide.addText(s[0], { x: 5.7, y: 2.45 + i * 0.68, w: 3.4, h: 0.28, fontSize: 14, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "left" });
    slide.addText("✓ " + s[1], { x: 5.7, y: 2.72 + i * 0.68, w: 3.4, h: 0.3, fontSize: 11, fontFace: "Microsoft YaHei", color: C.gray, align: "left" });
  });
  addDecorCircle(slide, 8.5, 0.8, 1.0, C.teal, 0.2);
  addDecorCircle(slide, 0.3, 4.2, 0.7, C.coral, 0.2);
  slide.addShape(pres.shapes.ROUNDED_RECTANGLE, { x: 0.8, y: 4.7, w: 8.4, h: 0.45, fill: { color: C.orangeLight }, rectRadius: 0.1 });
  slide.addText("学你想学，教你想教，一个用时间币串起技能供需的互助社区。", { x: 0.8, y: 4.7, w: 8.4, h: 0.45, fontSize: 13, fontFace: "Microsoft YaHei", color: C.orange, align: "center", valign: "middle", bold: true });
}

// ============ SLIDE 5: 团队分工 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "团队分工");

  // 画卡通头像的辅助函数
  function drawCartoonFace(slide, cx, cy, r, skin, hair, cheek) {
    // 头发（后层，半圆）
    slide.addShape(pres.shapes.OVAL, { x: cx - r * 1.08, y: cy - r * 1.0, w: r * 2.16, h: r * 1.3, fill: { color: hair } });
    // 脸（圆形）
    slide.addShape(pres.shapes.OVAL, { x: cx - r, y: cy - r, w: r * 2, h: r * 2, fill: { color: skin } });
    // 眼睛
    const eyeR = r * 0.09;
    slide.addShape(pres.shapes.OVAL, { x: cx - r * 0.35, y: cy - r * 0.18, w: eyeR * 2, h: eyeR * 2, fill: { color: "2C1810" } });
    slide.addShape(pres.shapes.OVAL, { x: cx + r * 0.35 - eyeR * 2, y: cy - r * 0.18, w: eyeR * 2, h: eyeR * 2, fill: { color: "2C1810" } });
    // 腮红
    const blushR = r * 0.1;
    slide.addShape(pres.shapes.OVAL, { x: cx - r * 0.55, y: cy + r * 0.08, w: blushR * 2, h: blushR * 1.5, fill: { color: cheek, transparency: 40 } });
    slide.addShape(pres.shapes.OVAL, { x: cx + r * 0.55 - blushR * 2, y: cy + r * 0.08, w: blushR * 2, h: blushR * 1.5, fill: { color: cheek, transparency: 40 } });
    // 微笑
    slide.addShape(pres.shapes.OVAL, { x: cx - r * 0.25, y: cy + r * 0.15, w: r * 0.5, h: r * 0.18, fill: { color: "E8907A", transparency: 30 } });
    // 遮挡微笑上半部分，只露弧线
    slide.addShape(pres.shapes.OVAL, { x: cx - r * 0.3, y: cy + r * 0.08, w: r * 0.6, h: r * 0.25, fill: { color: skin } });
  }

  const members = [
    { name: "郑朴", role: "项目负责人", skin: "FFE0C0", hair: "3D1C0A", cheek: "FF8A80", desc: "负责整体需求分析与功能规划\n制定开发计划与进度管理\n设计后端分层架构与多模块划分\n协调团队任务、把控项目方向", color: C.orange },
    { name: "吴仁杰", role: "后端+前端开发", skin: "FFD8B8", hair: "1A1A2E", cheek: "FF8A80", desc: "Spring Boot 7模块后端开发\nVue 3 + Element Plus前端实现\n14张数据库表结构设计\nAPI接口开发与前后端联调", color: C.teal },
    { name: "黄煜乾", role: "文档+PPT", skin: "FFE8D0", hair: "4A3020", cheek: "FF8A80", desc: "SRS需求规格说明编写\n详细设计文档与ER图绘制\n答辩PPT制作与视觉设计\n部署说明文档编写", color: C.coral },
    { name: "胡子风", role: "测试", skin: "FFDCC0", hair: "2C1810", cheek: "FF8A80", desc: "功能测试用例编写与执行\nAPI接口测试与回归验证\nBug发现、跟踪与修复确认\n用户体验测试与改进建议", color: C.teal },
  ];
  members.forEach((m, i) => {
    const col = i % 2, row = Math.floor(i / 2);
    const x = 0.8 + col * 4.3, y = 1.25 + row * 1.95;
    addCard(slide, x, y, 3.9, 1.7);
    // 卡通头像
    drawCartoonFace(slide, x + 0.6, y + 0.75, 0.58, m.skin, m.hair, m.cheek);
    // 名字+角色
    slide.addText(m.name, { x: x + 1.3, y: y + 0.35, w: 2.3, h: 0.35, fontSize: 17, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "left" });
    slide.addText(m.role, { x: x + 1.3, y: y + 0.68, w: 2.3, h: 0.25, fontSize: 12, fontFace: "Microsoft YaHei", color: m.color, bold: true, align: "left" });
    // 描述
    slide.addText(m.desc, { x: x + 0.3, y: y + 1.05, w: 3.3, h: 0.6, fontSize: 10, fontFace: "Microsoft YaHei", color: C.gray, align: "left", lineSpacingMultiple: 1.3 });
  });
  addBottomLine(slide, "4人协作团队  ·  前后端分离开发  ·  明确分工、高效协作、共同推进");
}

// ============ SLIDE 6: 核心功能 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "核心功能");
  slide.addText("覆盖技能交易全流程，从发布到评价的闭环体验", { x: 0.8, y: 1.0, w: 8.4, h: 0.35, fontSize: 12, fontFace: "Microsoft YaHei", color: C.gray, align: "left" });

  function drawFeatureIcon(slide, cx, cy, r, char, color) {
    // 彩色外圈
    slide.addShape(pres.shapes.OVAL, { x: cx - r, y: cy - r, w: r * 2, h: r * 2, fill: { color }, line: { color, width: 0 } });
    // 白色内圆
    const innerR = r * 0.82;
    slide.addShape(pres.shapes.OVAL, { x: cx - innerR, y: cy - innerR, w: innerR * 2, h: innerR * 2, fill: { color: C.white } });
    // 中心文字
    slide.addText(char, { x: cx - r, y: cy - r, w: r * 2, h: r * 2, fontSize: r * 1.1, fontFace: "Microsoft YaHei", color, bold: true, align: "center", valign: "middle" });
  }

  const features = [
    { char: "广", title: "技能广场", desc: "分类浏览、关键词搜索\n支持技能发布、编辑与下架\n热门排序与分页加载", color: C.orange },
    { char: "赏", title: "需求悬赏", desc: "发布需求并设置时间币悬赏\n多人申请，发布者挑选并验收\n验收通过即转币，安全有保障", color: C.teal },
    { char: "币", title: "时间银行", desc: "钱包余额实时查询\n时间币收入/支出流水\n下单冻结、取消解冻自动管理", color: C.coral },
    { char: "单", title: "订单交易", desc: "下单→待确认→进行中→验收→完成\n5态状态机严格流转\n双方协商、取消退款机制完善", color: C.teal },
    { char: "评", title: "双盲评价", desc: "交易后双方互评，写完可见对方评价\n7天自动解盲机制\n四维信誉雷达图：按时/沟通/专业/态度", color: C.orange },
    { char: "管", title: "管理后台", desc: "用户管理（启用/禁用）\n技能审核（通过/拒绝）\n申诉处理与公告发布管理", color: C.coral },
  ];
  features.forEach((f, i) => {
    const col = i % 3, row = Math.floor(i / 3);
    const cx = 2.05 + col * 3.0, cy = 1.88 + row * 1.95;
    const x = cx - 1.325, y = cy - 0.85;
    addCard(slide, x, y, 2.65, 1.7);
    // 绘制图标
    drawFeatureIcon(slide, cx, y + 0.55, 0.33, f.char, f.color);
    // 标题
    slide.addText(f.title, { x: x + 0.1, y: y + 0.95, w: 2.45, h: 0.3, fontSize: 14, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "center" });
    // 描述
    slide.addText(f.desc, { x: x + 0.15, y: y + 1.25, w: 2.35, h: 0.42, fontSize: 9.5, fontFace: "Microsoft YaHei", color: C.gray, align: "center", lineSpacingMultiple: 1.3 });
  });
}

// ============ SLIDE 7: 功能展示 — 用户端 (3x2 large screenshots) ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "功能展示 — 用户端");

  const userPages = [
    { img: "user-home.png", label: "技能广场首页" },
    { img: "skill-detail.png", label: "技能详情" },
    { img: "messages.png", label: "私信聊天" },
    { img: "wallet.png", label: "时间钱包" },
    { img: "orders-buyer.png", label: "订单详情" },
    { img: "profile.png", label: "个人中心" },
  ];

  userPages.forEach((p, i) => {
    const col = i % 3, row = Math.floor(i / 3);
    const x = 0.7 + col * 3.0, y = 1.2 + row * 2.15;
    const iw = 2.65, ih = 1.7;
    // Card background
    slide.addShape(pres.shapes.ROUNDED_RECTANGLE, { x: x - 0.05, y: y - 0.05, w: iw + 0.1, h: ih + 0.5, fill: { color: C.white }, shadow: makeCardShadow(), rectRadius: 0.1 });
    // Image
    addImg(slide, p.img, x, y, iw, ih);
    // Border
    slide.addShape(pres.shapes.RECTANGLE, { x, y, w: iw, h: ih, fill: { color: "FFFFFF", transparency: 100 }, line: { color: "F0E8E0", width: 0.5 } });
    // Label below image
    slide.addText(p.label, { x, y: y + ih + 0.05, w: iw, h: 0.35, fontSize: 12, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "center", valign: "middle" });
  });
}

// ============ SLIDE 8: 功能展示 — 管理后台 (2x2 large screenshots) ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "功能展示 — 管理后台");

  const adminPages = [
    { img: "admin-users.png", label: "用户管理", desc: "启用/禁用用户账号" },
    { img: "admin-skills.png", label: "技能审核", desc: "通过/拒绝技能发布" },
    { img: "admin-appeals.png", label: "申诉处理", desc: "处理用户交易申诉" },
    { img: "admin-announcements.png", label: "公告管理", desc: "发布/编辑系统公告" },
  ];

  adminPages.forEach((p, i) => {
    const col = i % 2, row = Math.floor(i / 2);
    const x = 0.7 + col * 4.65, y = 1.2 + row * 2.2;
    const iw = 4.0, ih = 1.65;
    // Card background
    slide.addShape(pres.shapes.ROUNDED_RECTANGLE, { x: x - 0.05, y: y - 0.05, w: iw + 0.1, h: ih + 0.55, fill: { color: C.white }, shadow: makeCardShadow(), rectRadius: 0.1 });
    // Image
    addImg(slide, p.img, x, y, iw, ih);
    // Border
    slide.addShape(pres.shapes.RECTANGLE, { x, y, w: iw, h: ih, fill: { color: "FFFFFF", transparency: 100 }, line: { color: "F0E8E0", width: 0.5 } });
    // Label
    slide.addText(p.label, { x, y: y + ih + 0.03, w: iw, h: 0.3, fontSize: 13, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "center", valign: "middle" });
    slide.addText(p.desc, { x, y: y + ih + 0.28, w: iw, h: 0.2, fontSize: 9.5, fontFace: "Microsoft YaHei", color: C.gray, align: "center" });
  });
}

// ============ SLIDE 9: 章节页 02 ============
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addDecorCircle(slide, -1.0, 3.0, 3.5, C.teal, 0.15);
  addDecorCircle(slide, 2.0, -0.5, 1.8, C.teal, 0.2);
  addDecorRoundedRect(slide, 0.5, 1.0, 0.08, 1.6, C.teal, 0.8);
  slide.addText("02", { x: 1.0, y: 1.2, w: 2.5, h: 1.4, fontSize: 80, fontFace: "Microsoft YaHei", color: C.teal, bold: true, align: "left" });
  slide.addText("技术架构与核心设计", { x: 3.5, y: 1.5, w: 5.5, h: 0.8, fontSize: 32, fontFace: "Microsoft YaHei", color: C.darkTeal, bold: true, align: "left" });
  slide.addText("TECHNICAL ARCHITECTURE & DESIGN", { x: 3.5, y: 2.3, w: 5.5, h: 0.4, fontSize: 14, fontFace: "Arial", color: C.gray, align: "left" });
}

// SLIDE 10: Tech architecture
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "技术架构总览");

  const drawBox = (x, y, w, h, label, color, sublabel) => {
    slide.addShape(pres.shapes.ROUNDED_RECTANGLE, { x, y, w, h, fill: { color, transparency: 12 }, line: { color, width: 1.5 }, rectRadius: 0.08 });
    slide.addText(label, { x, y, w, h, fontSize: 11, fontFace: "Microsoft YaHei", color: C.white, bold: true, align: "center", valign: "middle" });
    if (sublabel) slide.addText(sublabel, { x, y: y + h - 0.15, w, h: 0.15, fontSize: 7, fontFace: "Arial", color: "E0E0E0", align: "center" });
  };

  drawBox(1.0, 1.2, 3.5, 0.7, "Vue 3 + Vite 8 用户前端", C.orange, "localhost:5173");
  drawBox(5.5, 1.2, 3.5, 0.7, "Vue 3 管理后台", C.orange, "localhost:5174");
  slide.addText("▼", { x: 0, y: 1.95, w: 10, h: 0.3, fontSize: 14, color: C.gray, align: "center", valign: "middle" });
  drawBox(2.5, 2.25, 5.0, 0.7, "skill-gateway  统一入口 :8080", C.teal);
  slide.addText("▼", { x: 0, y: 3.0, w: 10, h: 0.3, fontSize: 14, color: C.gray, align: "center", valign: "middle" });

  ["common", "user", "skill", "order", "wallet", "admin"].forEach((m, i) => {
    slide.addShape(pres.shapes.ROUNDED_RECTANGLE, { x: 1.0 + i * 1.4, y: 3.3, w: 1.2, h: 0.55, fill: { color: C.orange, transparency: 75 }, line: { color: C.orange, width: 1 }, rectRadius: 0.06 });
    slide.addText(m, { x: 1.0 + i * 1.4, y: 3.3, w: 1.2, h: 0.55, fontSize: 10, fontFace: "Arial", color: C.dark, bold: true, align: "center", valign: "middle" });
  });

  slide.addText("▼", { x: 0, y: 3.9, w: 10, h: 0.3, fontSize: 14, color: C.gray, align: "center", valign: "middle" });
  drawBox(2.5, 4.2, 5.0, 0.55, "MySQL 8.0  (14张表)", C.darkTeal);

  ["Spring Boot 3.2", "MyBatis-Plus 3.5.5", "JWT认证", "Element Plus", "Pinia", "Axios"].forEach((t, i) => {
    const tx = 0.8 + i * 1.55;
    slide.addShape(pres.shapes.ROUNDED_RECTANGLE, { x: tx, y: 5.05, w: 1.35, h: 0.32, fill: { color: C.cardBg }, line: { color: "F0E8E0", width: 0.5 }, rectRadius: 0.08 });
    slide.addText(t, { x: tx, y: 5.05, w: 1.35, h: 0.32, fontSize: 9, fontFace: "Arial", color: C.gray, align: "center", valign: "middle" });
  });
}

// SLIDE 11: Key highlights
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "关键技术亮点");
  const highlights = [
    { title: "JWT认证体系", color: C.orange, desc: "自定义@RequireAuth注解 + 全局拦截器\n登录态管理，角色权限控制\nToken自动续期与失效处理" },
    { title: "订单状态机", color: C.teal, desc: "待确认→进行中→待确认完成→已完成/已取消\n时间币自动冻结/解冻/转账\n双方确认机制保障交易安全" },
    { title: "雪花ID处理", color: C.coral, desc: "Snowflake算法生成分布式唯一ID\nJackson Long→String序列化\n彻底解决前端JavaScript精度丢失问题" },
    { title: "双盲评价机制", color: C.teal, desc: "双方互评后立即可见评价内容\n7天超时自动解盲\n四维信誉雷达图(按时/沟通/专业/态度)" },
  ];
  highlights.forEach((h, i) => {
    const col = i % 2, row = Math.floor(i / 2);
    const x = 0.8 + col * 4.3, y = 1.3 + row * 2.0;
    addCard(slide, x, y, 3.9, 1.75);
    slide.addShape(pres.shapes.RECTANGLE, { x, y, w: 0.07, h: 1.75, fill: { color: h.color } });
    slide.addText(h.title, { x: x + 0.25, y: y + 0.15, w: 3.4, h: 0.35, fontSize: 16, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "left" });
    slide.addText(h.desc, { x: x + 0.25, y: y + 0.6, w: 3.4, h: 1.0, fontSize: 11.5, fontFace: "Microsoft YaHei", color: C.gray, align: "left", lineSpacingMultiple: 1.5 });
  });
}

// SLIDE 12: Database
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "数据库设计");
  addCard(slide, 0.8, 1.3, 5.0, 3.5);
  const tables = ["user (用户)", "skill (技能)", "skill_category (分类)", "bounty (悬赏)", "bounty_application (申请)", "skill_order (订单)", "time_transaction (流水)", "review (评价)", "appeal (申诉)", "announcement (公告)", "private_message (私信)", "chat_message (聊天)", "user_follow (关注)", "notification (通知)"];
  tables.forEach((t, i) => {
    slide.addText("▸  " + t, { x: 1.1 + (i % 2) * 2.4, y: 1.45 + Math.floor(i / 2) * 0.44, w: 2.3, h: 0.4, fontSize: 11, fontFace: "Microsoft YaHei", color: C.dark, align: "left" });
  });
  slide.addText("技术要点", { x: 6.2, y: 1.3, w: 3.0, h: 0.4, fontSize: 16, fontFace: "Microsoft YaHei", color: C.teal, bold: true, align: "left" });
  const points = [
    { label: "雪花ID主键", desc: "分布式唯一，趋势递增" },
    { label: "BCrypt加密", desc: "密码安全存储，防彩虹表" },
    { label: "范式设计 (3NF)", desc: "消除数据冗余，保证一致性" },
    { label: "BaseEntity", desc: "公共字段：id/createTime/updateTime" },
  ];
  points.forEach((p, i) => {
    const y = 1.85 + i * 0.75;
    addCard(slide, 6.2, y, 3.2, 0.6);
    slide.addShape(pres.shapes.RECTANGLE, { x: 6.2, y, w: 0.06, h: 0.6, fill: { color: C.teal } });
    slide.addText(p.label, { x: 6.45, y: y + 0.05, w: 2.7, h: 0.28, fontSize: 12, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "left" });
    slide.addText(p.desc, { x: 6.45, y: y + 0.3, w: 2.7, h: 0.25, fontSize: 10, fontFace: "Microsoft YaHei", color: C.gray, align: "left" });
  });
  addBottomLine(slide, "共14张表，完整覆盖业务需求");
}

// SLIDE 13: Section divider 03
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addDecorCircle(slide, -1.0, 3.0, 3.5, C.coral, 0.15);
  addDecorCircle(slide, 2.0, -0.3, 1.8, C.coral, 0.2);
  addDecorRoundedRect(slide, 0.5, 1.0, 0.08, 1.6, C.coral, 0.8);
  slide.addText("03", { x: 1.0, y: 1.2, w: 2.5, h: 1.4, fontSize: 80, fontFace: "Microsoft YaHei", color: C.coral, bold: true, align: "left" });
  slide.addText("项目总结与展望", { x: 3.5, y: 1.5, w: 5.5, h: 0.8, fontSize: 32, fontFace: "Microsoft YaHei", color: C.darkTeal, bold: true, align: "left" });
  slide.addText("SUMMARY & OUTLOOK", { x: 3.5, y: 2.3, w: 5.5, h: 0.4, fontSize: 14, fontFace: "Arial", color: C.gray, align: "left" });
}

// SLIDE 14: Results & AI
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addTitle(slide, "项目成果与AI辅助开发");
  addCard(slide, 0.8, 1.3, 3.9, 3.35);
  slide.addText("项目成果", { x: 1.1, y: 1.45, w: 3.4, h: 0.4, fontSize: 18, fontFace: "Microsoft YaHei", color: C.orange, bold: true, align: "left" });
  ["7个Maven模块全部编译通过", "14张数据库表完整设计", "13+前端页面完整实现", "30+ API接口全覆盖", "8项课程交付物全部完成"].forEach((a, i) => {
    slide.addText("✓", { x: 1.1, y: 2.05 + i * 0.5, w: 0.3, h: 0.35, fontSize: 14, color: C.orange, align: "center" });
    slide.addText(a, { x: 1.5, y: 2.05 + i * 0.5, w: 3.0, h: 0.35, fontSize: 13, fontFace: "Microsoft YaHei", color: C.dark, align: "left" });
  });
  addCard(slide, 5.3, 1.3, 3.9, 3.35);
  slide.addText("AI辅助开发", { x: 5.6, y: 1.45, w: 3.4, h: 0.4, fontSize: 18, fontFace: "Microsoft YaHei", color: C.teal, bold: true, align: "left" });
  const aiItems = [
    { label: "全流程AI辅助", desc: "使用Claude Code从零搭建项目" },
    { label: "多Agent协作", desc: "开发/测试/文档Agent并行工作" },
    { label: "排坑驱动迭代", desc: "30+排坑记录，持续优化代码质量" },
    { label: "融入课程要求", desc: "AI辅助开发满足课程设计规范" },
  ];
  aiItems.forEach((a, i) => {
    const sy = 2.0 + i * 0.62;
    slide.addShape(pres.shapes.OVAL, { x: 5.6, y: sy + 0.06, w: 0.18, h: 0.18, fill: { color: C.teal } });
    slide.addText(a.label, { x: 5.95, y: sy, w: 3.0, h: 0.28, fontSize: 12, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "left" });
    slide.addText(a.desc, { x: 5.95, y: sy + 0.25, w: 3.0, h: 0.25, fontSize: 10.5, fontFace: "Microsoft YaHei", color: C.gray, align: "left" });
  });
  slide.addShape(pres.shapes.ROUNDED_RECTANGLE, { x: 0.8, y: 4.9, w: 8.4, h: 0.45, fill: { color: C.tealLight }, rectRadius: 0.1 });
  slide.addText("课程要求全部满足：分层架构  |  前后端分离  |  14张表  |  JWT认证  |  Git版本管理  |  AI融入", { x: 0.8, y: 4.9, w: 8.4, h: 0.45, fontSize: 11, fontFace: "Microsoft YaHei", color: C.teal, align: "center", valign: "middle", bold: true });
}

// SLIDE 15: Thank you
{
  const slide = pres.addSlide();
  slide.background = { color: C.bg };
  addDecorCircle(slide, -0.8, -0.5, 3.0, C.teal, 0.15);
  addDecorCircle(slide, -0.3, 2.5, 2.0, C.orange, 0.12);
  addDecorCircle(slide, 7.5, -0.5, 3.0, C.orange, 0.12);
  addDecorCircle(slide, 8.5, 2.0, 2.5, C.teal, 0.10);
  slide.addText("THANK YOU", { x: 0, y: 1.2, w: 10, h: 1.2, fontSize: 56, fontFace: "Microsoft YaHei", color: C.dark, bold: true, align: "center" });
  slide.addText("感谢聆听", { x: 0, y: 2.5, w: 10, h: 0.7, fontSize: 30, fontFace: "Microsoft YaHei", color: C.coral, bold: true, align: "center" });
  slide.addShape(pres.shapes.RECTANGLE, { x: 3.5, y: 3.5, w: 3.0, h: 0.03, fill: { color: C.lightGray } });
  slide.addText("指导教师：刘德才", { x: 0, y: 3.85, w: 10, h: 0.45, fontSize: 16, fontFace: "Microsoft YaHei", color: C.gray, align: "center" });
  slide.addText("第10组全体成员：郑朴  ·  吴仁杰  ·  黄煜乾  ·  胡子风", { x: 0, y: 4.3, w: 10, h: 0.4, fontSize: 14, fontFace: "Microsoft YaHei", color: C.lightGray, align: "center" });
  slide.addText("Prometheus——技能时间银行  ·  2026", { x: 0, y: 4.8, w: 10, h: 0.35, fontSize: 11, fontFace: "Microsoft YaHei", color: C.lightGray, align: "center" });
}

// ============ OUTPUT ============
pres.writeFile({ fileName: "D:/Agent工作区/Claude Code工作区/综合实训项目/document/答辩PPT-第10组.pptx" })
  .then(() => console.log("PPT with screenshots generated successfully!"))
  .catch(err => console.error("Error:", err));
