export interface Prescription {
    id?: number;
    patientId: number;
    doctorId: number;
    patientName?: string;
    doctorName?: string;
    medication: string;
    dosage?: string;
    instructions?: string;
    prescribedDate?: string;
}
