<template>
	<view>
		<CustomNavBar :showBack="true" title="探索未知" @back="handleBack" />
		<map name="" class="map" :polyline="lines"></map>
	</view>
</template>

<script setup>
import {
	ref
} from "vue"
import CustomNavBar from '@/components/CustomNavBar.vue'

const handleBack = () => {
	uni.navigateBack({
		delta: 1
	});
}
const lines = ref([
	{
		points: [
			{ latitude: 39.907675, longitude: 116.397193 },
			{ latitude: 39.907627, longitude: 116.39539500000001 },
			{ latitude: 39.907516, longitude: 116.391906 },
			{ latitude: 39.907388000000005, longitude: 116.38882500000001 },
			{ latitude: 39.907262, longitude: 116.38409300000001 },
			{ latitude: 39.907239000000004, longitude: 116.38281300000001 },
			{ latitude: 39.907239000000004, longitude: 116.38281300000001 },
			{ latitude: 39.907484000000004, longitude: 116.38278100000001 },
			{ latitude: 39.907484000000004, longitude: 116.38278100000001 },
			{ latitude: 39.907239000000004, longitude: 116.38281300000001 },
			{ latitude: 39.907239000000004, longitude: 116.38281300000001 },
			{ latitude: 39.907074, longitude: 116.38281 },
			{ latitude: 39.907185, longitude: 116.38754300000001 },
			{ latitude: 39.907424, longitude: 116.39539500000001 },
			{ latitude: 39.907555, longitude: 116.39936 },
			{ latitude: 39.907626, longitude: 116.400936 },
			{ latitude: 39.907682, longitude: 116.402507 },
			{ latitude: 39.907784, longitude: 116.404191 },
			{ latitude: 39.907872, longitude: 116.40682799999999 },
			{ latitude: 39.907947, longitude: 116.40950899999999 },
			{ latitude: 39.907988, longitude: 116.41169899999998 },
			{ latitude: 39.908082, longitude: 116.41530399999998 },
			{ latitude: 39.9081, longitude: 116.41582399999997 },
			{ latitude: 39.908172, longitude: 116.41736799999997 },
			{ latitude: 39.908189, longitude: 116.41811999999997 }
		],
		width: 10,
		color: '#D9EEFB',
		arrowLine: true
	}
])
</script>

<style lang="scss">
.map {
	height: 1500rpx;
	width: 100%;
}
</style>