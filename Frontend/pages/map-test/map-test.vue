<template>
	<view>
		<map name="" class="map" :polyline="lines"></map>
	</view>
</template>

<script setup>
import {
	ref
} from "vue"
import {
	onLoad
} from "@dcloudio/uni-app"
import {
	routeApi
} from "@/common/api/index.js"

const lines = ref([])

// 从后端加载地图路线（/api/routes）
const loadRoutes = async () => {
	try {
		const res = await routeApi.list()
		lines.value = (res || []).map(r => ({
			points: r.points || [],
			width: r.width || 10,
			color: r.color || '#D9EEFB',
			arrowLine: r.arrowLine !== false
		}))
	} catch (e) {
		// 错误提示已统一处理
	}
}

onLoad(() => {
	loadRoutes()
})
</script>

<style lang="scss">
.map {
	height: 600rpx;
	width: 100%;
}
</style>