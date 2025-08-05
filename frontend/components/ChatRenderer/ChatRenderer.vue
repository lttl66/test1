<template>
  <div class="chat-renderer">
    <!-- Text Content -->
    <div v-if="format === 'TEXT'" class="text-content">
      <div v-html="formattedText" class="text-body"></div>
    </div>

    <!-- Card Content -->
    <div v-else-if="format === 'CARD'" class="card-content">
      <el-card class="content-card" shadow="hover">
        <div slot="header" class="card-header" v-if="cardData.title">
          <span class="card-title">{{ cardData.title }}</span>
          <span v-if="cardData.subtitle" class="card-subtitle">{{ cardData.subtitle }}</span>
        </div>
        
        <div class="card-body">
          <div v-if="cardData.imageUrl" class="card-image">
            <el-image
              :src="cardData.imageUrl"
              fit="cover"
              :preview-src-list="[cardData.imageUrl]"
              class="image"
            />
          </div>
          
          <div v-if="cardData.description" class="card-description">
            <p v-html="formatText(cardData.description)"></p>
          </div>
          
          <div v-if="cardData.data" class="card-data">
            <el-descriptions :column="2" size="small" border>
              <el-descriptions-item
                v-for="(value, key) in cardData.data"
                :key="key"
                :label="formatLabel(key)"
              >
                {{ formatValue(value) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>

        <div v-if="cardData.actions && cardData.actions.length > 0" class="card-actions">
          <el-button
            v-for="action in cardData.actions"
            :key="action.action"
            :type="getActionType(action.type)"
            size="small"
            @click="handleAction(action)"
          >
            <i v-if="getActionIcon(action.type)" :class="getActionIcon(action.type)"></i>
            {{ action.label }}
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- List Content -->
    <div v-else-if="format === 'LIST'" class="list-content">
      <div v-if="listData.title" class="list-header">
        <h4>{{ listData.title }}</h4>
      </div>
      
      <el-timeline class="content-timeline">
        <el-timeline-item
          v-for="(item, index) in listData.items"
          :key="index"
          :icon="item.iconUrl ? '' : 'el-icon-info'"
          class="timeline-item"
        >
          <template v-if="item.iconUrl" slot="dot">
            <el-avatar :size="24" :src="item.iconUrl" />
          </template>
          
          <div class="timeline-content">
            <div class="item-header">
              <h5 v-if="item.title" class="item-title">{{ item.title }}</h5>
              <span v-if="item.subtitle" class="item-subtitle">{{ item.subtitle }}</span>
            </div>
            
            <p v-if="item.description" class="item-description" v-html="formatText(item.description)"></p>
            
            <div v-if="item.data" class="item-data">
              <el-tag
                v-for="(value, key) in item.data"
                :key="key"
                size="mini"
                class="data-tag"
              >
                {{ formatLabel(key) }}: {{ formatValue(value) }}
              </el-tag>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>

    <!-- Table Content -->
    <div v-else-if="format === 'TABLE'" class="table-content">
      <div v-if="tableData.title" class="table-header">
        <h4>{{ tableData.title }}</h4>
      </div>
      
      <el-table
        :data="tableRows"
        stripe
        border
        size="small"
        class="content-table"
        :max-height="300"
      >
        <el-table-column
          v-for="(header, index) in tableData.headers"
          :key="index"
          :prop="`col${index}`"
          :label="header"
          :min-width="100"
          show-overflow-tooltip
        >
          <template slot-scope="scope">
            <span v-html="formatTableCell(scope.row[`col${index}`])"></span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Rich Media Content -->
    <div v-else-if="format === 'RICH_MEDIA'" class="rich-media-content">
      <div class="media-container">
        <!-- Handle different media types -->
        <div v-if="mediaData.type === 'image'" class="media-image">
          <el-image
            :src="mediaData.url"
            fit="contain"
            :preview-src-list="[mediaData.url]"
            class="image"
          />
          <p v-if="mediaData.caption" class="media-caption">{{ mediaData.caption }}</p>
        </div>
        
        <div v-else-if="mediaData.type === 'video'" class="media-video">
          <video
            :src="mediaData.url"
            controls
            class="video"
            :poster="mediaData.thumbnail"
          ></video>
          <p v-if="mediaData.caption" class="media-caption">{{ mediaData.caption }}</p>
        </div>
        
        <div v-else-if="mediaData.type === 'chart'" class="media-chart">
          <div ref="chartContainer" class="chart-container"></div>
          <p v-if="mediaData.caption" class="media-caption">{{ mediaData.caption }}</p>
        </div>
        
        <div v-else class="media-fallback">
          <el-alert
            title="Unsupported Media Type"
            :description="`Media type '${mediaData.type}' is not supported`"
            type="warning"
            :closable="false"
          />
        </div>
      </div>
    </div>

    <!-- Custom Content -->
    <div v-else-if="format === 'CUSTOM'" class="custom-content">
      <component
        v-if="customComponent"
        :is="customComponent"
        :data="content"
        :metadata="metadata"
        @action="handleAction"
      />
      <div v-else class="custom-fallback">
        <pre>{{ JSON.stringify(content, null, 2) }}</pre>
      </div>
    </div>

    <!-- Fallback for unknown formats -->
    <div v-else class="fallback-content">
      <el-alert
        title="Unknown Content Format"
        :description="`Format '${format}' is not supported`"
        type="warning"
        :closable="false"
      />
      <details class="debug-info">
        <summary>Debug Information</summary>
        <pre>{{ JSON.stringify({ content, format, metadata }, null, 2) }}</pre>
      </details>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatRenderer',
  
  props: {
    content: {
      type: [String, Object, Array],
      required: true
    },
    format: {
      type: String,
      default: 'TEXT'
    },
    metadata: {
      type: Object,
      default: () => ({})
    }
  },

  computed: {
    formattedText() {
      if (typeof this.content === 'string') {
        return this.formatText(this.content)
      }
      return String(this.content)
    },

    cardData() {
      if (typeof this.content === 'object' && this.content !== null) {
        return this.content
      }
      return {
        title: 'Information',
        description: String(this.content)
      }
    },

    listData() {
      if (typeof this.content === 'object' && this.content.items) {
        return this.content
      }
      return {
        title: 'Items',
        items: Array.isArray(this.content) ? this.content : [{ title: String(this.content) }]
      }
    },

    tableData() {
      if (typeof this.content === 'object' && this.content.headers && this.content.rows) {
        return this.content
      }
      return {
        title: 'Data',
        headers: ['Item', 'Value'],
        rows: [[String(this.content), '']]
      }
    },

    tableRows() {
      return this.tableData.rows.map(row => {
        const rowObj = {}
        row.forEach((cell, index) => {
          rowObj[`col${index}`] = cell
        })
        return rowObj
      })
    },

    mediaData() {
      if (typeof this.content === 'object' && this.content.type) {
        return this.content
      }
      return {
        type: 'unknown',
        url: String(this.content)
      }
    },

    customComponent() {
      // Return custom component based on metadata or content type
      if (this.metadata.component) {
        return this.metadata.component
      }
      return null
    }
  },

  mounted() {
    if (this.format === 'RICH_MEDIA' && this.mediaData.type === 'chart') {
      this.renderChart()
    }
  },

  methods: {
    formatText(text) {
      if (!text) return ''
      
      // Convert markdown-like formatting to HTML
      return text
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/`(.*?)`/g, '<code>$1</code>')
        .replace(/\n/g, '<br>')
        .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')
    },

    formatLabel(key) {
      return key.replace(/([A-Z])/g, ' $1')
        .replace(/^./, str => str.toUpperCase())
        .trim()
    },

    formatValue(value) {
      if (value === null || value === undefined) return 'N/A'
      if (typeof value === 'boolean') return value ? 'Yes' : 'No'
      if (typeof value === 'object') return JSON.stringify(value)
      if (typeof value === 'number') {
        return value.toLocaleString()
      }
      return String(value)
    },

    formatTableCell(value) {
      return this.formatText(this.formatValue(value))
    },

    getActionType(actionType) {
      const typeMap = {
        'primary': 'primary',
        'secondary': 'default',
        'success': 'success',
        'warning': 'warning',
        'danger': 'danger',
        'link': 'text'
      }
      return typeMap[actionType] || 'default'
    },

    getActionIcon(actionType) {
      const iconMap = {
        'link': 'el-icon-link',
        'function': 'el-icon-setting',
        'navigation': 'el-icon-right',
        'download': 'el-icon-download',
        'external': 'el-icon-top-right'
      }
      return iconMap[actionType] || null
    },

    handleAction(action) {
      this.$emit('action', {
        type: action.type,
        action: action.action,
        parameters: action.parameters,
        label: action.label
      })
    },

    renderChart() {
      // Placeholder for chart rendering
      // In a real implementation, you would use a charting library like Chart.js or ECharts
      this.$nextTick(() => {
        if (this.$refs.chartContainer && this.mediaData.data) {
          // Example implementation would go here
          this.$refs.chartContainer.innerHTML = `
            <div style="text-align: center; padding: 20px; border: 1px dashed #ccc;">
              <p>Chart would be rendered here</p>
              <small>Chart data: ${JSON.stringify(this.mediaData.data)}</small>
            </div>
          `
        }
      })
    }
  }
}
</script>

<style scoped>
.chat-renderer {
  width: 100%;
  max-width: 100%;
}

/* Text Content */
.text-content {
  line-height: 1.6;
}

.text-body {
  word-wrap: break-word;
  white-space: pre-wrap;
}

.text-body ::v-deep code {
  background: #f5f5f5;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 0.9em;
}

.text-body ::v-deep a {
  color: #409eff;
  text-decoration: none;
}

.text-body ::v-deep a:hover {
  text-decoration: underline;
}

/* Card Content */
.card-content {
  margin: 8px 0;
}

.content-card {
  max-width: 100%;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-title {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.card-subtitle {
  font-size: 14px;
  color: #909399;
}

.card-body {
  padding: 0;
}

.card-image {
  margin-bottom: 12px;
}

.card-image .image {
  width: 100%;
  max-height: 200px;
  border-radius: 4px;
}

.card-description {
  margin-bottom: 12px;
  line-height: 1.6;
}

.card-data {
  margin-bottom: 12px;
}

.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

/* List Content */
.list-content {
  margin: 8px 0;
}

.list-header h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-weight: 600;
}

.content-timeline {
  padding-left: 0;
}

.timeline-content {
  padding-left: 8px;
}

.item-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}

.item-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.item-subtitle {
  font-size: 12px;
  color: #909399;
}

.item-description {
  margin: 0 0 8px 0;
  line-height: 1.5;
  color: #606266;
}

.item-data {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.data-tag {
  font-size: 11px;
}

/* Table Content */
.table-content {
  margin: 8px 0;
}

.table-header h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-weight: 600;
}

.content-table {
  width: 100%;
  font-size: 12px;
}

/* Rich Media Content */
.rich-media-content {
  margin: 8px 0;
}

.media-container {
  text-align: center;
}

.media-image .image,
.media-video .video {
  max-width: 100%;
  border-radius: 4px;
}

.media-caption {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.chart-container {
  min-height: 200px;
  width: 100%;
}

/* Custom Content */
.custom-content {
  margin: 8px 0;
}

.custom-fallback {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  overflow-x: auto;
}

/* Fallback Content */
.fallback-content {
  margin: 8px 0;
}

.debug-info {
  margin-top: 8px;
  font-size: 12px;
}

.debug-info summary {
  cursor: pointer;
  color: #909399;
}

.debug-info pre {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  font-size: 11px;
  overflow-x: auto;
  margin-top: 4px;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .card-actions {
    flex-direction: column;
  }
  
  .card-actions .el-button {
    width: 100%;
  }
  
  .content-table {
    font-size: 11px;
  }
  
  .item-data {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>