import { Component } from '@angular/core';
import { ApiService } from '../service/api.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';



@Component({
  selector: 'app-findbooking',
  imports: [CommonModule, FormsModule],
  templateUrl: './findbooking.component.html',
  styleUrl: './findbooking.component.css'
})
export class FindbookingComponent {

  constructor(private apiService: ApiService){}
  
  confirmationCode: string = '';
  bookingDetails: any = null;
  error: any = null;

  handleSearch(){
    if (!this.confirmationCode.trim()) {
      this.showError("Por favor, introduzca el código de confirmación de la reserva");
      return;
    }

    this.apiService.getBookingByReference(this.confirmationCode).subscribe({
      next: (res) => {
        this.bookingDetails = res.booking;
        this.bookingDetails.room.imageUrl =
        'http://localhost:7070' + this.bookingDetails.room.imageUrl;
      },
      error: (err) => {
        this.showError(err?.error.message || "Error al obtener los detalles de la reserva")
      },
    })
  }

  showError(err: any): void{
    console.log(err)
    this.error = err;
    setTimeout(() => {
      this.error = ''
    }, 4000)
  }

}
