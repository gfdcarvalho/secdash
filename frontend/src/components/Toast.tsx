import Style from './Toast.module.css'

type ToastProps = {
    message: string
    onClose: () => void
    duration?: number
}

export function Toast({ message, onClose, duration = 3000 }: ToastProps) {
    return (
        <div
            className={Style.toast}
            style={{ animationDuration: `${duration}ms` }}
            onAnimationEnd={onClose}
        >
            <span>{message}</span>
            <button className={Style.closeButton} onClick={onClose}>✕</button>
        </div>
    )
}
