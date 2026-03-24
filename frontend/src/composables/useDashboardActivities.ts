import { ref, computed, onMounted } from 'vue'
import { activityService, type Activity, type ActivityParticipation } from '../api/activityService'

export function useDashboardActivities() {
  const myActivities = ref<Activity[]>([])
  const pendingActivities = ref<ActivityParticipation[]>([])
  const isLoadingActivities = ref(false)
  const isLoadingPending = ref(false)
  const errorActivities = ref<string | null>(null)
  const errorPending = ref<string | null>(null)

  const fetchMyActivities = async () => {
    isLoadingActivities.value = true
    errorActivities.value = null
    try {
      myActivities.value = await activityService.getMyActivities()
    } catch (error) {
      errorActivities.value = 'Fehler beim Laden der Aktivitäten'
      console.error(error)
    } finally {
      isLoadingActivities.value = false
    }
  }

  const fetchPendingActivities = async () => {
    isLoadingPending.value = true
    errorPending.value = null
    try {
      pendingActivities.value = await activityService.getPendingActivities()
    } catch (error) {
      errorPending.value = 'Fehler beim Laden der ausstehenden Aktivitäten'
      console.error(error)
    } finally {
      isLoadingPending.value = false
    }
  }

  const acceptActivity = async (activityId: number) => {
    try {
      await activityService.acceptActivity(activityId)
      pendingActivities.value = pendingActivities.value.filter(a => a.activityId !== activityId)
      await fetchMyActivities()
    } catch (error) {
      errorPending.value = 'Fehler beim Akzeptieren der Aktivität'
      console.error(error)
    }
  }

  const declineActivity = async (activityId: number) => {
    try {
      await activityService.declineActivity(activityId)
      pendingActivities.value = pendingActivities.value.filter(a => a.activityId !== activityId)
    } catch (error) {
      errorPending.value = 'Fehler beim Ablehnen der Aktivität'
      console.error(error)
    }
  }

  const hasPendingActivities = computed(() => pendingActivities.value.length > 0)
  const pendingActivitiesCount = computed(() => pendingActivities.value.length)

  const upcomingActivities = computed(() => {
    const now = new Date()
    return myActivities.value
      .filter(a => new Date(a.startTime) > now)
      .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime())
  })

  onMounted(() => {
    fetchMyActivities()
    fetchPendingActivities()
  })

  return {
    myActivities,
    pendingActivities,
    isLoadingActivities,
    isLoadingPending,
    errorActivities,
    errorPending,
    fetchMyActivities,
    fetchPendingActivities,
    acceptActivity,
    declineActivity,
    hasPendingActivities,
    pendingActivitiesCount,
    upcomingActivities
  }
}
