import { ref, computed, onMounted } from 'vue'
import { activityService, type Activity, type ActivityParticipation } from '../api/activityService'
import { authService } from '../api/authService'

export function useDashboardActivities() {
  const myActivities = ref<Activity[]>([])
  const pendingActivities = ref<ActivityParticipation[]>([])
  const isLoadingActivities = ref(false)
  const isLoadingPending = ref(false)
  const errorActivities = ref<string | null>(null)
  const errorPending = ref<string | null>(null)
  const currentUserId = ref<number | null>(null)

  const ensureCurrentUserId = async (): Promise<number | null> => {
    if (currentUserId.value !== null) return currentUserId.value

    try {
      const profile = await authService.getMyProfile()
      const parsedId = Number((profile as { id?: unknown }).id)
      currentUserId.value = Number.isFinite(parsedId) ? parsedId : null
      return currentUserId.value
    } catch {
      currentUserId.value = null
      return null
    }
  }

  const fetchMyActivities = async () => {
    isLoadingActivities.value = true
    errorActivities.value = null

    try {
      myActivities.value = await activityService.getUpcomingActivities()
    } catch (error) {
      errorActivities.value = 'Fehler beim Laden der Aktivitaeten'
      console.error(error)
    } finally {
      isLoadingActivities.value = false
    }
  }

  const fetchPendingActivities = async () => {
    isLoadingPending.value = true
    errorPending.value = null

    try {
      const userId = await ensureCurrentUserId()
      if (userId === null) {
        pendingActivities.value = []
        errorPending.value = 'Nutzerprofil konnte nicht ermittelt werden'
        return
      }

      const sourceActivities =
        myActivities.value.length > 0 ? myActivities.value : await activityService.getUpcomingActivities()

      const pendingRows: Array<ActivityParticipation | null> = await Promise.all(
        sourceActivities.map(async (activity) => {
          const participants = await activityService.getParticipants(activity.id)
          const ownParticipant = participants.find((participant) => Number(participant.userId) === userId)
          const ownStatus = String(ownParticipant?.status ?? 'PENDING').toUpperCase()

          if (ownStatus !== 'PENDING') return null

          return {
            id: activity.id,
            activityId: activity.id,
            activityTitle: activity.title,
            status: 'PENDING',
            startTime: activity.startTime,
            endTime: activity.endTime
          }
        })
      )

      pendingActivities.value = pendingRows.filter((row): row is ActivityParticipation => row !== null)
    } catch (error) {
      errorPending.value = 'Fehler beim Laden der ausstehenden Aktivitaeten'
      console.error(error)
    } finally {
      isLoadingPending.value = false
    }
  }

  const acceptActivity = async (activityId: number) => {
    try {
      await activityService.respondToActivity(activityId, 'ACCEPTED')
      pendingActivities.value = pendingActivities.value.filter((a) => a.activityId !== activityId)
      await fetchMyActivities()
    } catch (error) {
      errorPending.value = 'Fehler beim Akzeptieren der Aktivitaet'
      console.error(error)
    }
  }

  const declineActivity = async (activityId: number) => {
    try {
      await activityService.respondToActivity(activityId, 'DECLINED')
      pendingActivities.value = pendingActivities.value.filter((a) => a.activityId !== activityId)
    } catch (error) {
      errorPending.value = 'Fehler beim Ablehnen der Aktivitaet'
      console.error(error)
    }
  }

  const hasPendingActivities = computed(() => pendingActivities.value.length > 0)
  const pendingActivitiesCount = computed(() => pendingActivities.value.length)

  const upcomingActivities = computed(() => {
    const now = new Date()
    return myActivities.value
      .filter((a) => new Date(a.startTime) > now)
      .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime())
  })

  onMounted(() => {
    void fetchMyActivities()
    void fetchPendingActivities()
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
