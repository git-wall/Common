// ============================================
// GLOBAL STATE
// ============================================
let profilesData = {};
let currentApi = '';
let selectedVersions = [];

// ============================================
// LOAD PROFILES
// ============================================
async function loadProfiles() {
  const input = document.getElementById('folderInput');
  const files = input.files;

  if (files.length === 0) return;

  // Clear previous state
  document.getElementById('emptyState').innerHTML = '<div class="loading">⏳ Đang load và validate dữ liệu...</div>';
  document.getElementById('emptyState').style.display = 'flex';
  document.getElementById('sidebar').classList.remove('show');
  document.getElementById('versionSection').classList.remove('show');
  document.getElementById('compareSection').classList.remove('show');
  document.getElementById('tabs').classList.remove('show');

  profilesData = {};
  selectedVersions = [];
  currentApi = '';

  const errors = [];
  const stats = { validFiles: 0, invalidFiles: 0, apis: new Set(), versions: new Set() };

  // Parse all files
  for (let file of files) {
    const path = file.webkitRelativePath;
    const parts = path.split('/');

    if (parts.length < 4) {
      stats.invalidFiles++;
      continue;
    }

    const apiName = parts[1];
    const version = parts[2];
    const fileName = parts[3];

    // Validate version format (v1, v2, v3...)
    if (!version.match(/^v\d+$/)) {
      errors.push(`Version không đúng format: ${path}`);
      stats.invalidFiles++;
      continue;
    }

    // Validate file name
    const validFiles = ['avg.json', 'min.json', 'max.json', 'metadata.json'];
    if (!validFiles.includes(fileName)) {
      errors.push(`File không hợp lệ: ${path}`);
      stats.invalidFiles++;
      continue;
    }

    if (!profilesData[apiName]) profilesData[apiName] = {};
    if (!profilesData[apiName][version]) profilesData[apiName][version] = {};

    try {
      const content = await file.text();
      const data = JSON.parse(content);

      // Validate JSON structure
      if (fileName === 'metadata.json') {
        if (!data.api || !data.count || data.avgTimeMs === undefined) {
          throw new Error('thiếu field bắt buộc');
        }
      } else {
        if (!data.method || data.timeMs === undefined || data.memoryKb === undefined) {
          throw new Error('thiếu field bắt buộc');
        }
      }

      if (fileName === 'avg.json') profilesData[apiName][version].avg = data;
      else if (fileName === 'min.json') profilesData[apiName][version].min = data;
      else if (fileName === 'max.json') profilesData[apiName][version].max = data;
      else if (fileName === 'metadata.json') profilesData[apiName][version].metadata = data;

      stats.validFiles++;
      stats.apis.add(apiName);
      stats.versions.add(version);

    } catch (e) {
      errors.push(`Lỗi parse ${path}: ${e.message}`);
      stats.invalidFiles++;
    }
  }

  // Validate completeness
  for (let api in profilesData) {
    for (let version in profilesData[api]) {
      const v = profilesData[api][version];
      if (!v.avg || !v.min || !v.max || !v.metadata) {
        errors.push(`${api}/${version} thiếu file`);
      }
    }
  }

  // Check results
  if (Object.keys(profilesData).length === 0) {
    showErrorModal('Không tìm thấy dữ liệu hợp lệ!', [
      'Vui lòng kiểm tra cấu trúc folder',
      'Click nút "Guide" để xem hướng dẫn',
      '',
      '<strong>Các lỗi:</strong>',
      ...errors
    ]);
    document.getElementById('emptyState').style.display = 'flex';
    return;
  }

  if (errors.length > 0) {
    console.warn('Có lỗi khi load:', errors);
    showErrorModal(
      `⚠️ Load thành công nhưng có ${errors.length} lỗi`,
      [
        `✅ Loaded: ${stats.apis.size} APIs, ${stats.versions.size} versions`,
        `❌ Bỏ qua: ${stats.invalidFiles} files không hợp lệ`,
        '',
        ...errors.slice(0, 20)
      ],
      'warning'
    );
  }

  console.log('✅ Loaded:', Array.from(stats.apis));

  document.getElementById('emptyState').style.display = 'none';
  populateApiList();
}

// ============================================
// API SELECTION
// ============================================
function populateApiList() {
  const container = document.getElementById('apiList');
  container.innerHTML = '';

  Object.keys(profilesData).sort().forEach(api => {
    const div = document.createElement('div');
    div.className = 'api-item';
    div.textContent = api;
    div.onclick = () => selectApi(api);
    container.appendChild(div);
  });

  document.getElementById('sidebar').classList.add('show');
}

function selectApi(api) {
  currentApi = api;
  selectedVersions = [];

  // Update active state
  document.querySelectorAll('.api-item').forEach(item => {
    item.classList.remove('active');
    if (item.textContent === api) {
      item.classList.add('active');
    }
  });

  populateVersionList();
  document.getElementById('versionSection').classList.add('show');
  document.getElementById('compareSection').classList.add('show');
  document.getElementById('tabs').classList.remove('show');
}

// ============================================
// VERSION SELECTION
// ============================================
function populateVersionList() {
  const container = document.getElementById('versionList');
  container.innerHTML = '';

  const versions = Object.keys(profilesData[currentApi]).sort((a, b) => {
    return parseInt(a.substring(1)) - parseInt(b.substring(1));
  });

  versions.forEach(ver => {
    const div = document.createElement('div');
    div.className = 'version-item';
    div.textContent = ver;
    div.onclick = () => toggleVersion(ver, div);
    container.appendChild(div);
  });

  updateSelectedCount();
}

function toggleVersion(version, element) {
  if (element.classList.contains('selected')) {
    element.classList.remove('selected');
    selectedVersions = selectedVersions.filter(v => v !== version);
  } else {
    if (selectedVersions.length >= 3) {
      alert('Chỉ được chọn tối đa 3 versions!');
      return;
    }
    element.classList.add('selected');
    selectedVersions.push(version);
  }

  console.log('Selected:', selectedVersions);
  updateSelectedCount();
}

function updateSelectedCount() {
  document.getElementById('selectedCount').textContent = selectedVersions.length;
  document.getElementById('compareBtn').disabled = selectedVersions.length === 0;
}

function showComparison() {
  if (selectedVersions.length === 0) return;
  document.getElementById('tabs').classList.add('show');
  switchTab('avg');
}

// ============================================
// TAB SWITCHING
// ============================================
function switchTab(mode) {
  document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
  event.target.classList.add('active');

  document.querySelectorAll('.view').forEach(view => view.classList.remove('active'));
  document.getElementById(mode + 'View').classList.add('active');

  if (mode === 'summary') {
    renderSummary();
  } else {
    renderTreeView(mode);
  }
}

// ============================================
// TREE VIEW RENDERING
// ============================================
function renderTreeView(mode) {
  const container = document.getElementById(mode + 'Trees');
  container.innerHTML = '';

  const baselineVersion = selectedVersions[0];
  const baselineData = profilesData[currentApi][baselineVersion][mode];

  selectedVersions.forEach((version, idx) => {
    const data = profilesData[currentApi][version][mode];
    const metadata = profilesData[currentApi][version].metadata;
    const isBaseline = idx === 0;

    const panel = document.createElement('div');
    panel.className = 'tree-panel';

    const headerClass = isBaseline ? 'tree-panel-header baseline' : 'tree-panel-header';
    const headerLabel = isBaseline
      ? `${version} - ${mode.toUpperCase()} (BASELINE ⭐)`
      : `${version} - ${mode.toUpperCase()} (vs ${baselineVersion})`;

    panel.innerHTML = `
            <div class="${headerClass}">${headerLabel}</div>
            <div class="tree-panel-body" id="${mode}-${version}-tree"></div>
            <div class="tree-info">📊 Samples: ${metadata.count} calls</div>
        `;

    container.appendChild(panel);

    const treeBody = document.getElementById(`${mode}-${version}-tree`);
    renderTree(data, treeBody, '', true, isBaseline ? null : baselineData);
  });
}

function renderTree(node, container, prefix, isLast, baselineNode) {
  const line = document.createElement('div');
  line.className = 'tree-line';

  const branch = isLast ? '└─ ' : '├─ ';
  let timeDisplay = `${node.timeMs}ms`;
  let memDisplay = `${node.memoryKb}KB`;

  if (baselineNode) {
    const timeDiff = node.timeMs - baselineNode.timeMs;
    const memDiff = node.memoryKb - baselineNode.memoryKb;

    // Time comparison
    if (timeDiff < 0) {
      const percent = Math.abs((timeDiff / baselineNode.timeMs) * 100).toFixed(1);
      timeDisplay = `<span class="improved">${node.timeMs}ms ↓${percent}% ✓</span>`;
    } else if (timeDiff > 0) {
      const percent = ((timeDiff / baselineNode.timeMs) * 100).toFixed(1);
      timeDisplay = `<span class="degraded">${node.timeMs}ms ↑${percent}% ✗</span>`;
    } else {
      timeDisplay = `<span class="unchanged">${node.timeMs}ms =</span>`;
    }

    // Memory comparison
    if (memDiff < 0) {
      const percent = Math.abs((memDiff / baselineNode.memoryKb) * 100).toFixed(1);
      memDisplay = `<span class="improved">${node.memoryKb}KB ↓${percent}% ✓</span>`;
    } else if (memDiff > 0) {
      const percent = ((memDiff / baselineNode.memoryKb) * 100).toFixed(1);
      memDisplay = `<span class="degraded">${node.memoryKb}KB ↑${percent}% ✗</span>`;
    } else {
      memDisplay = `<span class="unchanged">${node.memoryKb}KB =</span>`;
    }
  } else {
    timeDisplay = `<span class="time">${timeDisplay}</span>`;
    memDisplay = `<span class="memory">${memDisplay}</span>`;
  }

  line.innerHTML = `${prefix}${branch}<span class="method-name">${node.method}</span> (${timeDisplay}, ${memDisplay})`;
  container.appendChild(line);

  if (node.children && node.children.length > 0) {
    const childPrefix = prefix + (isLast ? '   ' : '│  ');
    node.children.forEach((child, index) => {
      const isLastChild = index === node.children.length - 1;
      const baselineChild = baselineNode?.children?.[index];
      renderTree(child, container, childPrefix, isLastChild, baselineChild);
    });
  }
}

// ============================================
// SUMMARY VIEW RENDERING
// ============================================
function renderSummary() {
  const versions = selectedVersions.map(ver => ({
    version: ver,
    ...profilesData[currentApi][ver].metadata
  }));

  const timeRanking = [...versions].sort((a, b) => a.avgTimeMs - b.avgTimeMs);
  renderRankingTable('timeRankingTable', timeRanking, 'avgTimeMs');

  const memRanking = [...versions].sort((a, b) => a.avgMemKb - b.avgMemKb);
  renderRankingTable('memoryRankingTable', memRanking, 'avgMemKb');

  renderDetailsTable('timeDetailsTable', versions, 'Time');
  renderDetailsTable('memoryDetailsTable', versions, 'Mem');

  renderRecommendation(timeRanking, memRanking);
}

function renderRankingTable(tableId, data, field) {
  const tbody = document.getElementById(tableId).querySelector('tbody');
  tbody.innerHTML = '';

  data.forEach((item, idx) => {
    const rankClass = idx === 0 ? 'rank-1' : idx === 1 ? 'rank-2' : 'rank-3';
    let badge = '';

    if (idx === 0) {
      badge = '<span class="improvement-badge badge-best">✓ Best</span>';
    } else {
      const diff = ((item[field] - data[0][field]) / data[0][field] * 100).toFixed(1);
      badge = `<span class="improvement-badge badge-bad">+${diff}%</span>`;
    }

    tbody.innerHTML += `
            <tr>
                <td><span class="${rankClass} rank-badge">${idx + 1}</span></td>
                <td><strong>${item.version}</strong></td>
                <td class="metric-cell">${item[field]}</td>
                <td>${badge}</td>
            </tr>
        `;
  });
}

function renderDetailsTable(tableId, versions, type) {
  const tbody = document.getElementById(tableId).querySelector('tbody');
  tbody.innerHTML = '';

  versions.forEach(v => {
    const min = type === 'Time' ? v.minTimeMs : v.minMemKb;
    const avg = type === 'Time' ? v.avgTimeMs : v.avgMemKb;
    const max = type === 'Time' ? v.maxTimeMs : v.maxMemKb;
    const range = max - min;

    tbody.innerHTML += `
            <tr>
                <td><strong>${v.version}</strong></td>
                <td class="metric-cell">${min}</td>
                <td class="metric-cell">${avg}</td>
                <td class="metric-cell">${max}</td>
                <td class="metric-cell">${range}</td>
            </tr>
        `;
  });
}

function renderRecommendation(timeRanking, memRanking) {
  const bestTime = timeRanking[0];
  const bestMem = memRanking[0];

  let recommendation = '';

  if (bestTime.version === bestMem.version) {
    recommendation = `
            <strong>🏆 Khuyến nghị:</strong> Version <strong>${bestTime.version}</strong>
            là tốt nhất về cả Time và Memory. Nên sử dụng cho production.<br><br>
            <strong>Range</strong> = Khoảng dao động (Max - Min). Số càng nhỏ = performance ổn định.
        `;
  } else {
    recommendation = `
            <strong>⚖️ Phân tích:</strong><br>
            - <strong>${bestTime.version}</strong> nhanh nhất (${bestTime.avgTimeMs}ms)<br>
            - <strong>${bestMem.version}</strong> tiết kiệm memory nhất (${bestMem.avgMemKb}KB)<br><br>
            <strong>💡 Khuyến nghị:</strong> Ưu tiên ${bestTime.version} nếu cần speed, hoặc ${bestMem.version} nếu giới hạn memory.<br><br>
            <strong>Range</strong> = Khoảng dao động (Max - Min). Số càng nhỏ = performance ổn định.
        `;
  }

  document.getElementById('recommendationNote').innerHTML = recommendation;
}

// ============================================
// MODAL FUNCTIONS
// ============================================
function showErrorModal(title, messages, type = 'error') {
  const modal = document.getElementById('errorModal');
  const titleEl = document.getElementById('errorTitle');
  const bodyEl = document.getElementById('errorBody');

  titleEl.innerHTML = type === 'warning' ? '⚠️ ' + title : '❌ ' + title;
  bodyEl.innerHTML = '<ul>' + messages.map(msg => `<li>${msg}</li>`).join('') + '</ul>';

  modal.classList.add('show');
}

function closeErrorModal() {
  document.getElementById('errorModal').classList.remove('show');
}

function showGuide() {
  document.getElementById('guideModal').classList.add('show');
}

function closeGuide() {
  document.getElementById('guideModal').classList.remove('show');
}

// Close modal when clicking outside
window.onclick = function(event) {
  if (event.target.classList.contains('modal')) {
    event.target.classList.remove('show');
  }
}
