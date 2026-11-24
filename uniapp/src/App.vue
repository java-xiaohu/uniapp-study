<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { setupRouteGuard } from '@/utils/routeGuard'

onLaunch(() => {
  setupRouteGuard()
})
</script>

<style>
page {
  --wot-color-theme: #ff6b00;
}
</style>
