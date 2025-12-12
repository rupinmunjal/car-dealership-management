import { Component } from '@angular/core';
import { DealerService } from '../../services/dealer';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-dealer-add',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './dealer-add.html',
  styleUrl: './dealer-add.css',
})
export class DealerAdd {

  dealer = {
    name: '',
    location: ''
  };

  message: string = '';
  error: string = '';

  constructor(
    private dealerService: DealerService,
    private router: Router
  ) {}

  onSubmit() {
    this.error = '';
    this.dealerService.addDealer(this.dealer).subscribe({
      next: () => {
        this.message = 'Dealer added successfully!';
        this.error = '';
        // Clear only the form data, not the messages
        this.clearForm();
        // Redirect after 1 second
        setTimeout(() => {
          this.router.navigate(['/dealers']);
        }, 1000);
      },
      error: (err) => {
        this.error = 'Failed to add dealer. Please try again.';
        this.message = '';
        console.error(err);
      }
    });
  }

  clearForm() {
    this.dealer = {
      name: '',
      location: ''
    };
  }

  resetForm() {
    this.clearForm();
    this.message = '';
    this.error = '';
  }
}
