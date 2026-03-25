import { ref, computed, onMounted } from 'vue'
import { activityService, type Activity, type ActivityParticipation } from '../api/activityService'
import { authService } from '../api/authService'

type ParticipationStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED'

export function useDashboardActivities() {
  const myActivities = ref<Activity[]>([])
  const pendingActivities = ref<ActivityParticipation[]>([])
  const ownActivityStatuses = ref<Record<number, ParticipationStatus>>({})
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

  const normalizeStatus = (value: unknown): ParticipationStatus => {
    const status = String(value ?? 'PENDING').toUpperCase()
    if (status === 'ACCEPTED') return 'ACCEPTED'
    if (status === 'DECLINED') return 'DECLINED'
    return 'PENDING'
  }

  const loadOwnStatusesForActivities = async (activities: Activity[]): Promise<void> => {
    const userId = await ensureCurrentUserId()
    if (userId === null) {
      ownActivityStatuses.value = {}
      return
    }

    const statusPairs = await Promise.all(
      activities.map(async (activity) => {
        const participants = await activityService.getParticipants(activity.id)
        const ownParticipant = participants.find((participant) => Number(participant.userId) === userId)
        return [activity.id, normalizeStatus(ownParticipant?.status)] as const
      })
    )

    ownActivityStatuses.value = Object.fromEntries(statusPairs)
  }

  const fetchMyActivities = async () => {
    isLoadingActivities.value = true
    errorActivities.value = null

    try {
      myActivities.value = await activityService.getUpcomingActivities()
      await loadOwnStatusesForActivities(myActivities.value)
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
      const sourceActivities =
        myActivities.value.length > 0 ? myActivities.value : await activityService.getUpcomingActivities()

      if (
        sourceActivities.length > 0 &&
        Object.keys(ownActivityStatuses.value).length !== sourceActivities.length
      ) {
        await loadOwnStatusesForActivities(sourceActivities)
      }

      pendingActivities.value = sourceActivities
        .filter((activity) => ownActivityStatuses.value[activity.id] === 'PENDING')
        .map((activity) => ({
          id: activity.id,
          activityId: activity.id,
          activityTitle: activity.title,
          status: 'PENDING',
          startTime: activity.startTime,
          endTime: activity.endTime
        }))
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
      ownActivityStatuses.value = { ...ownActivityStatuses.value, [activityId]: 'ACCEPTED' }
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
      ownActivityStatuses.value = { ...ownActivityStatuses.value, [activityId]: 'DECLINED' }
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
      .filter((a) => ownActivityStatuses.value[a.id] === 'ACCEPTED')
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
